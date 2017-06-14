package org.cloudbus.cloudsimdisk.examples.MyRing;

import com.google.common.io.Files;
import com.google.gson.Gson;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.cloudbus.cloudsimdisk.examples.MyConstants;
import org.cloudbus.cloudsimdisk.examples.Node;
import org.cloudbus.cloudsimdisk.models.hdd.*;
import org.cloudbus.cloudsimdisk.power.models.hdd.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by skulkarni9 on 2/26/17.
 */
public class MyRing implements Serializable
{
    Map<String,MyRegion> myRegions;
    long numberOfPartitions;
    long totalNumberOfPartitions;
    int replicas;
    int partitionShift;
    double overloadPartition;
    Map<Integer, List<MyNode>> partitionToReplicaToNode;
    Map<MyNode, List<Integer>> nodeToPartition;
    List<MyNode> allNodes;

    public MyRing(int partitonPower, int replicas, double overloadPercent)
    {
        this.myRegions = new HashMap();
        this.replicas = replicas;
        this.partitionShift = 32 - partitonPower;
        this.numberOfPartitions = (long) 1<<partitonPower;
        this.totalNumberOfPartitions = this.numberOfPartitions * this.replicas;
        this.overloadPartition = this.getTotalNumberOfPartitions() * (overloadPercent/100);
        nodeToPartition = new HashMap<>();
    }

    public static void saveRing(MyRing myRing, String filePath) {
        Gson gson = new Gson();
        String jsonStringObject = gson.toJson(myRing);
        try {
            byte[] data = SerializationUtils.serialize(myRing);
            FileUtils.writeByteArrayToFile(new File(filePath), data);
            //FileUtils.writeStringToFile(new File(filePath), jsonStringObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MyRing getRing(String filePath) {
        Gson gson = new Gson();
        MyRing myRing = null;
        try {
            byte data[] = FileUtils.readFileToByteArray(new File(filePath));
            myRing = (MyRing) SerializationUtils.deserialize(data);
            //String jsonString =  FileUtils.readFileToString(new File(filePath));
            //myRing = gson.fromJson(jsonString.toString(), MyRing.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myRing;
    }

    private void createRing()
    {
        this.calculatePartitions();
        this.initialzePartitionToReplicaToNode();
        this.allocatePartitionToReplicaToNode();
        this.createNodeToPartitionMapping();
        this.setAllNodes();
    }

    public Map<MyNode, List<Integer>> getNodeToPartition()
    {
        return this.nodeToPartition;
    }

    private void setAllNodes()
    {
        allNodes = new ArrayList<>();
        for(MyRegion myRegion : this.getAllRegions())
        {
            for(MyZone myZone : myRegion.getAllZones())
            {
                this.allNodes.addAll(myZone.getAllNodes());
            }
        }
    }

    public void setAllNodes(ArrayList<MyNode> complete_nodeList)
    {
        allNodes = complete_nodeList;
    }

    public List<MyNode> getAllNodes()
    {
        return this.allNodes;
    }

    private void createNodeToPartitionMapping()
    {
        for(Integer partition : this.partitionToReplicaToNode.keySet())
        {
            List<MyNode> myNodeList = this.partitionToReplicaToNode.get(partition);
            for(MyNode myNode : myNodeList)
            {
                if(this.nodeToPartition.containsKey(myNode))
                {
                    this.nodeToPartition.get(myNode).add(partition);
                }
                else
                {
                    List<Integer> partitionList = new ArrayList<>();
                    partitionList.add(partition);
                    this.nodeToPartition.put(myNode, partitionList);
                }
            }
        }
    }

    private void allocatePartitionToReplicaToNode()
    {
        MyRegion myRegion;
        MyZone myZone;
        MyNode myNode;

        for(int replica=0; replica<this.getReplicas(); replica++)
        {
            for(int partition=0; partition < this.getNumberOfPartitions(); partition++)
            {
                myRegion = this.getRegionByWeightDistribution(partition);
                myZone = this.getZoneByWeightDistribution(myRegion.getAllZones(), partition);
                myNode = this.getNodeByWeightDistribution(myZone.getAllNodes(), partition);
                myRegion.decrementPartition();
                myZone.decrementPartition();
                myNode.decrementPartition();
                myNode.addPartitionNames(partition);
                this.partitionToReplicaToNode.get(partition).add(myNode);
            }
        }
    }

    private long getUnsignedInt(int x)
    {
        return x & 0x00000000ffffffffL;
    }

    private long MD5(String md5)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes("UTF-8"));
            int result = ByteBuffer.wrap(array).order(ByteOrder.BIG_ENDIAN).getInt();
            return getUnsignedInt(result) >> this.partitionShift;
        }
        catch (Exception e)
        {
            new Exception("MD5 Failed");
        }
        return -1l;
    }

    public int getPartition(String filePath)
    {
        return (int)MD5(filePath);
    }

    public List<MyNode> getPrimaryNodes(String filePath)
    {
        int partition = (int)MD5(filePath);
        return this.partitionToReplicaToNode.get(partition);
    }

    private MyNode getNodeByWeightDistribution(Collection<MyNode> myNodeCollection, int partition)
    {
        List<MyNode> myNodeList = new ArrayList<>();
        for(MyNode myNode : myNodeCollection)
        {
            if(myNode.getNumberOfPartitionsByWeight() > 0 && !myNode.containsPartition(partition))
            {
                myNodeList.add(myNode);
            }
        }

        if(myNodeList.size() == 0)
        {
            for(MyNode myNode : myNodeCollection)
            {
                if(myNode.getNumberOfPartitionsByWeight() > 0)
                {
                    myNodeList.add(myNode);
                }
            }
        }

        List<Double> weightDistribution = new ArrayList<>();
        for(MyNode myNode : myNodeList)
        {
            weightDistribution.add(myNode.getNumberOfPartitionsByWeight());
        }
        int selectedIndex = this.getIndexSelectedByWeight(weightDistribution);
        return myNodeList.get(selectedIndex);
    }

    private MyZone getZoneByWeightDistribution(Collection<MyZone> myZoneCollection, int partition)
    {
        List<MyZone> myZoneList = new ArrayList<>();
        for(MyZone myZone : myZoneCollection)
        {
            if(myZone.getNumberOfPartitionsByWeight() > 0 && !myZone.containsPartition(partition))
            {
                myZoneList.add(myZone);
            }
        }
        if(myZoneList.size() == 0)
        {
            for(MyZone myZone : myZoneCollection)
            {
                if(myZone.getNumberOfPartitionsByWeight() > 0)
                {
                    myZoneList.add(myZone);
                }
            }
        }
        List<Double> weightDistribution = new ArrayList<>();
        for(MyZone myZone : myZoneList)
        {
            weightDistribution.add(myZone.getNumberOfPartitionsByWeight());
        }
        int selectedIndex = this.getIndexSelectedByWeight(weightDistribution);
        return myZoneList.get(selectedIndex);
    }

    private MyRegion getRegionByWeightDistribution(int partition)
    {
        List<MyRegion> allRegions = new ArrayList<>();
        for(MyRegion myRegion : this.getAllRegions())
        {
            if(myRegion.getNumberOfPartitionsByWeight() > 0 && !myRegion.contiansPartition(partition))
            {
                allRegions.add(myRegion);
            }
        }
        if(allRegions.size() == 0)
        {
            for(MyRegion myRegion : this.getAllRegions())
            {
                if(myRegion.getNumberOfPartitionsByWeight() > 0)
                {
                    allRegions.add(myRegion);
                }
            }
        }
        List<Double> weightDistribution = new ArrayList<>();
        for(MyRegion myRegion : allRegions)
        {
            weightDistribution.add(myRegion.getNumberOfPartitionsByWeight());
        }
        int selectedIndex = this.getIndexSelectedByWeight(weightDistribution);
        return allRegions.get(selectedIndex);
    }

    private int getIndexSelectedByWeight(List<Double> weights)
    {
        int lowIndex = 0;
        int highIndex = (int)weights.stream().mapToDouble(Double::doubleValue).sum();
        if(lowIndex == highIndex)
        {
            return lowIndex;
        }
        for(int i=1; i<weights.size(); i++)
        {
            weights.set(i, weights.get(i) + weights.get(i-1));
        }
        int randomNumber = ThreadLocalRandom.current().nextInt(lowIndex, highIndex);
        int index = 0;
        for(Double d : weights)
        {
            if(randomNumber >= d)
            {
                index++;
            }
            else
            {
                return index;
            }
        }
        return index;
    }

    private void initialzePartitionToReplicaToNode()
    {
        this.partitionToReplicaToNode = new HashMap<>();
        for(int partition=0; partition<this.numberOfPartitions; partition++)
        {
            this.partitionToReplicaToNode.put(partition, new ArrayList<>());
        }
    }

    public Collection<MyRegion> getAllRegions()
    {
        return this.myRegions.values();
    }

    public long getNumberOfPartitions()
    {
        return this.numberOfPartitions;
    }

    public long getTotalNumberOfPartitions()
    {
        return this.totalNumberOfPartitions;
    }

    public long getRemainingPartitions()
    {
        long partitions = 0L;
        for(MyRegion myRegion : this.getAllRegions())
        {
            partitions += myRegion.getNumberOfPartitionsByWeight();
        }
        return partitions;
    }

    public int getReplicas()
    {
        return this.replicas;
    }

    public void calculateNumberOfPartitionsByWeight()
    {
        for(MyRegion myRegion : this.getAllRegions())
        {
            myRegion.calculateNumberOfPartitionsByWeight(this.getTotalNumberOfPartitions(), this.getWeight());
        }
    }

    public void calculateNumberOfPartitionsByDispersion()
    {
        double regionPartitions = this.getTotalNumberOfPartitions()/this.getAllRegions().size();
        for(MyRegion myRegion : this.getAllRegions())
        {
            myRegion.calculateNumberOfPartitionsByDispersion(regionPartitions);
        }
    }

    public void calculateNumberOfPartitionsDifference()
    {
        for(MyRegion myRegion : this.getAllRegions())
        {
            myRegion.calculateNumberOfPartitionsDifference();
        }
    }

    public void calculatePartitions()
    {
        this.calculateNumberOfPartitionsByWeight();
        this.calculateNumberOfPartitionsByDispersion();
        this.calculateNumberOfPartitionsDifference();
    }

    public boolean containsByName(String regionName)
    {
        return this.myRegions.containsKey(regionName);
    }

    public MyRegion getRegionByName(String regionName)
    {
        return this.myRegions.get(regionName);
    }

    public void addRegion(MyRegion myRegion)
    {
        if(!this.containsByName(myRegion.getName()))
        {
            this.myRegions.put(myRegion.getName(), myRegion);
        }
    }

    public double getWeight() {
        double weight = 0.0;
        for(MyRegion myRegion : this.getAllRegions())
        {
            weight += myRegion.getWeight();
        }
        return weight;
    }

    public double getAverageWeight() {
        double weight = 0.0;
        for(MyRegion myRegion : this.getAllRegions())
        {
            weight += myRegion.getAverageWeight();
        }
        return weight/this.myRegions.size();
    }

    @Override
    public String toString()
    {
        return "Ring : " +this.myRegions.values().toString();
    }

    public static MyRing buildRing(String fileName, int nodeCount, int partitionPower, int replicas, double overloadPercent)
    {
        MyRing myRing = new MyRing(partitionPower, replicas, overloadPercent);
        File file = new File(fileName);
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            for(int line=0; line<nodeCount; line++)
            {
                String data[] = in.readLine().trim().split(",");
                String regionName = data[0];
                String zoneName = data[1];
                String nodeName = data[2];
                double weight = Double.parseDouble(data[3]);

                MyRegion myRegion;
                MyZone myZone;
                MyNode myNode;

                myRegion = myRing.containsByName(regionName) ? myRing.getRegionByName(regionName) : new MyRegion(regionName);
                myZone = myRegion.containsByName(zoneName) ? myRegion.getZoneByName(zoneName) : new MyZone(zoneName);

                myNode = new MyNode();
                myNode.setName(nodeName);
                myNode.setWeight(weight);
                myNode.setHddModel(MyConstants.STORAGE_MODEL_HDD);
                myNode.setHddPowerModel(MyConstants.STORAGE_POWER_MODEL_HDD);
                myNode.setSpunDown(false);

                myZone.addNode(myNode);
                myRegion.addZone(myZone);
                myRing.addRegion(myRegion);
            }
            myRing.createRing();
        }
        catch (Exception e)
        {
            new Exception("Main");
        }
        return myRing;
    }

    public static MyRing buildRing(String fileName, int nodeCount, int partitionPower, int replicas, double overloadPercent, boolean isStagingDisk, int
            diskType)
    {
        MyRing myRing = new MyRing(partitionPower, replicas, overloadPercent);
        File file = new File(fileName);
        // HDD specs
        StorageModelHdd[] storageModelHdds = new StorageModelHdd[]{ new StorageModelHddSeagateEnterpriseST6000VN0001() , new StorageModelHddHGSTUltrastarHUC109090CSS600(), new StorageModelHddToshibaEnterpriseMG04SCA500E() };
        PowerModelHdd[] powerModelHdds = new PowerModelHdd[]{ new PowerModeHddSeagateEnterpriseST6000VN0001() , new PowerModeHddHGSTUltrastarHUC109090CSS600() , new PowerModeHddToshibaEnterpriseMG04SCA500E() };


        // SSD specs
        StorageModelHdd[] storageModelSSDs = new StorageModelHdd[]{ new StorageModelSsdToshibaHG6EnterpriseTHNSNJ512GCSU() , new
                StorageModelSsdSeagate600ProEnterpriseST480FP0021(), new StorageModelSsdIntelDCS3500EnterpriseSC2BB800G401() };
        PowerModelHdd[] powerModelSSDs = new PowerModelHdd[]{ new PowerModelSsdToshibaHG6EnterpriseTHNSNJ512GCSU() , new
                PowerModelSsdSeagate600ProEnterpriseST480FP0021() , new PowerModelSsdIntelDCS3500EnterpriseSC2BB800G401() };

        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            for(int line=0; line<nodeCount; line++)
            {
                String data[] = in.readLine().trim().split(",");
                String regionName = data[0];
                String zoneName = data[1];
                String nodeName = data[2];
                double weight = Double.parseDouble(data[3]);

                MyRegion myRegion;
                MyZone myZone;
                MyNode myNode;

                myRegion = myRing.containsByName(regionName) ? myRing.getRegionByName(regionName) : new MyRegion(regionName);
                myZone = myRegion.containsByName(zoneName) ? myRegion.getZoneByName(zoneName) : new MyZone(zoneName);

                myNode = new MyNode();
                myNode.setName(nodeName);
                myNode.setWeight(weight);
                myNode.setSpunDown(false);

                myZone.addNode(myNode);
                myRegion.addZone(myZone);
                myRing.addRegion(myRegion);

                if(isStagingDisk){
                    /*
                    myNode.setHddModel(storageModelSSDs[line%3]);
                    myNode.setHddPowerModel(powerModelSSDs[line%3]);
                    */
                    // for benchmarking purpose keeping power, storage model across all SSDs constant
                    myNode.setHddModel(storageModelSSDs[diskType]);
                    myNode.setHddPowerModel(powerModelSSDs[diskType]);

                }
                else {
                    /*
                    myNode.setHddModel(storageModelHdds[line%3]);
                    myNode.setHddPowerModel(powerModelHdds[line%3]);
                    */
                    // for benchmarking purpose keeping power, storage model across all HDDs constant
                    myNode.setHddModel(storageModelHdds[diskType]);
                    myNode.setHddPowerModel(powerModelHdds[diskType]);
                }
            }
            myRing.createRing();
        }
        catch (Exception e)
        {
            new Exception("Main");
        }
        return myRing;
    }

    public static void main(String[] args)
    {
        int nodeCount = 8;
        int partitionPower = 4;
        int replicas = 3;
        double overloadPercent = 10.0;
        String ringInputPath = "modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/MyRing/rings.txt";
        MyRing myRing = buildRing(ringInputPath,
                nodeCount, partitionPower, replicas, overloadPercent);
        saveRing(myRing, "saved_ring_data.json");
        myRing = getRing("saved_ring_data.json");
        System.out.println(myRing);
        System.out.println("Total Weight : "+myRing.getWeight());
        System.out.println("Average Weight : "+myRing.getAverageWeight());
        System.out.println("Total Number of Partitions : "+myRing.getTotalNumberOfPartitions());

        display(myRing);

        displayPartitionMap(myRing.partitionToReplicaToNode);

        displayNodeMap(myRing.nodeToPartition);

        System.out.println("Partition is : "+myRing.getPartition("File/Path/here"));
        System.out.println("Nodes are : "+myRing.getPrimaryNodes("File/Path/here").toString());
    }

    public static void displayNodeMap(Map<MyNode, List<Integer>> nodeToPartition)
    {
        System.out.println("*********************");
        List<MyNode> keys = new ArrayList<>(nodeToPartition.keySet());
        Collections.sort(keys, Comparator.comparing(Object::toString));
        for(MyNode myNode : keys)
        {
            System.out.println("Node : "+myNode+", Partitions : "+nodeToPartition.get(myNode));
        }
        System.out.println("*********************\n\n");
    }

    public static void displayPartitionMap(Map<Integer, List<MyNode>> partitionToReplicaToNode)
    {
        System.out.println("*********************");
        List<Integer> keys = new ArrayList<>(partitionToReplicaToNode.keySet());
        keys.stream().mapToInt(Integer::intValue).sorted();
        for(Integer key : keys)
        {
            System.out.println("partition : "+key+", Replicas in Node : "+partitionToReplicaToNode.get(key).toString());
        }
        System.out.println("*********************\n\n");
    }

    public static void display(MyRing myRing)
    {
        System.out.println("*********************");
        for(MyRegion myRegion : myRing.getAllRegions())
        {
            System.out.println(myRegion.getName()+", ByWeight : "+myRegion.getNumberOfPartitionsByWeight()+
                    ", ByDispersion : "+myRegion.getNumberOfPartitionsByDispersion()+", Diff : "+myRegion.getNumberOfPartitionsDifference());
            for(MyZone myZone : myRegion.getAllZones())
            {
                System.out.println("\t"+myZone.getName()+", ByWeight : "+myZone.getNumberOfPartitionsByWeight()+
                        ", ByDispersion : "+myZone.getNumberOfPartitionsByDispersion()+" , Diff : "+myZone.getNumberOfPartitionsDifference());
                for(MyNode myNode : myZone.getAllNodes())
                {
                    System.out.println("\t\t"+myNode.getName()+", ByWeight : "+myNode.getNumberOfPartitionsByWeight()+
                            ", ByDispersion : "+myNode.getNumberOfPartitionsByDispersion()+", Diff : "+myNode.getNumberOfPartitionsDifference());
                }
            }
        }
        System.out.println("*********************\n\n");
    }
}
