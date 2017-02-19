package org.cloudbus.cloudsimdisk.examples;

import java.util.*;
import java.lang.*;
import java.security.*;
import java.nio.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

import org.cloudbus.cloudsimdisk.models.hdd.StorageModelHdd;
import org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd;

/*
    Ring
        - public List<Node> getHandOffNodes(String filePath)
        - public void addHandOffNodesEntry(String filePath, ArrayList<Node> nodes)
        - public void removeHandOffNodesEntry(String filePath)

        - public List<Node> getPrimaryNodesNodes(String filePath)
        - public void addPrimaryNodesEntry(String filePath, ArrayList<Node> nodes)
        - public void removePrimaryNodesEntry(String filePath)

        - public Map<String, List<Node>> getActiveNodes(String filePath)
        - public ArrayList<Node> getNodes(String filePath)
 */
public class Ring
{
    private HashMap<Integer, Node> nodes;
    private ArrayList<Integer> partitionToNode;
    private int replicas;
    private int partitionShift;
    private Map<Integer, List<Node>> zoneIdToNodes;
    private Map<String, List<Node>> handOffNodesMap;
    private Map<String, List<Node>> primaryNodesMap;

    public Ring(HashMap<Integer,Node> nodes, ArrayList<Integer> partitionToNode, int replicas)
    {
        this.nodes = nodes;
        this.partitionToNode = partitionToNode;
        this.replicas = replicas;
        
        int partitionPower = 1;
        while(Math.pow(2.0, (double)partitionPower) < nodes.size())
        {
            partitionPower++;
        }
        if(partitionToNode.size() != Math.pow(2.0, (double)partitionPower))
        {
            new Exception("partitionToPower is not power of 2");
        }
        this.partitionShift = 32 - partitionPower;
        this.zoneIdToNodes = this.getZoneIdToNodes();
        this.handOffNodesMap = new HashMap<>();
        this.primaryNodesMap = new HashMap<>();
    }
    
    public long getUnsignedInt(int x)
    {
        return x & 0x00000000ffffffffL;
    }
    
    public long MD5(String md5)
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
    
    public ArrayList<Node> getNodes(String filePath)
    {
        int part = (int)MD5(filePath);
        ArrayList<Integer> node_ids = new ArrayList<Integer>();
        node_ids.add(this.partitionToNode.get(part));
        ArrayList<Node> zones = new ArrayList<Node>();
        zones.add(this.nodes.get(node_ids.get(0)));
        for(int replica=1; replica<this.replicas; replica++)
        {
            while(node_ids.contains(this.partitionToNode.get(part)) && 
                zones.contains(this.nodes.get(this.partitionToNode.get(part))))
            {
                part++;
                if(part >= this.partitionToNode.size())
                {
                    part = 0;
                }
            }
            node_ids.add(this.partitionToNode.get(part));
            zones.add(this.nodes.get(node_ids.get(node_ids.size()-1)));
        }
        ArrayList<Node> results = new ArrayList<Node>();
        for(int i : node_ids)
        {
            results.add(this.nodes.get(i));
        } 
        return results;
    }

    public Map<Integer, List<Node>> getZoneIdToNodes()
    {
        Map<Integer, List<Node>> zoneIdToNodes = new HashMap<>();
        for(Node n : this.getAllNodes())
        {
            if(zoneIdToNodes.containsKey(n.getZone()))
            {
                zoneIdToNodes.get(n.getZone()).add(n);
            }
            else
            {
                List<Node> newNodeList = new ArrayList<>();
                newNodeList.add(n);
                zoneIdToNodes.put(n.getZone(), newNodeList);
            }
        }
        return zoneIdToNodes;
    }

    public List<Node> getHandOffNodes(String filePath)
    {
        if(this.handOffNodesMap.containsKey(filePath))
        {
            return this.handOffNodesMap.get(filePath);
        }
        return new ArrayList<>();
    }

    public void addHandOffNodesEntry(String filePath, ArrayList<Node> nodes)
    {
        if(this.handOffNodesMap.containsKey(filePath))
        {
            this.handOffNodesMap.get(filePath).addAll(nodes);
        }
        else
        {
            this.handOffNodesMap.put(filePath, nodes);
        }
    }

    public void removeHandOffNodesEntry(String filePath)
    {
        if(this.handOffNodesMap.containsKey(filePath))
        {
            this.handOffNodesMap.remove(filePath);
        }
    }

    public List<Node> getPrimaryNodesNodes(String filePath)
    {
        if(this.primaryNodesMap.containsKey(filePath))
        {
            return this.primaryNodesMap.get(filePath);
        }
        return new ArrayList<>();
    }

    public void addPrimaryNodesEntry(String filePath, ArrayList<Node> nodes)
    {
        if(this.primaryNodesMap.containsKey(filePath))
        {
            this.primaryNodesMap.get(filePath).addAll(nodes);
        }
        else
        {
            this.primaryNodesMap.put(filePath, nodes);
        }
    }

    public void removePrimaryNodesEntry(String filePath)
    {
        if(this.primaryNodesMap.containsKey(filePath))
        {
            this.primaryNodesMap.remove(filePath);
        }
    }
    
    public Map<String, List<Node>> getActiveNodes(String filePath)
    {
        List<Node> primaryNodes = new ArrayList<>();
        List<Node> currentHandOffNodes = new ArrayList<>();
        Set<Integer> zones = new HashSet<>();
        Map<String, List<Node>> result = new HashMap<>();
        for(Node n : getNodes(filePath))
        {
            if(!n.getIsSpunDown())
            {
                primaryNodes.add(n);
                zones.add(n.getZone());
            }
        }
        if(primaryNodes.size() != 3)
        {
            if(this.handOffNodesMap.containsKey(filePath))
            {
                int NumOfHandOffNodes = 3 - primaryNodes.size();
                if(NumOfHandOffNodes < this.handOffNodesMap.get(filePath).size())
                {
                    for(int i=0; i<NumOfHandOffNodes; i++)
                    {
                        currentHandOffNodes.add(this.handOffNodesMap.get(filePath).get(i));
                    }
                }
                else
                {
                    currentHandOffNodes.addAll(this.handOffNodesMap.get(filePath));
                }
            }
            int tries = 0;
            while (primaryNodes.size() + currentHandOffNodes.size() != 3 && tries != 1000)
            {
                Set<Integer> otherZones = this.zoneIdToNodes.keySet();
                otherZones.removeAll(zones);
                ArrayList<Integer> otherZonesList = new ArrayList<>(otherZones);
                if(otherZonesList.size() != 0)
                {
                    int randomNumber = ThreadLocalRandom.current().nextInt(0, otherZonesList.size());
                    List<Node> nodesFromRandomZone = this.zoneIdToNodes.get(otherZonesList.get(randomNumber));
                    Collections.shuffle(nodesFromRandomZone);
                    for(Node n : nodesFromRandomZone)
                    {
                        if(!n.getIsSpunDown())
                        {
                            currentHandOffNodes.add(n);
                            break;
                        }
                    }
                }
                else if(zones.size() != 0)
                {
                    List newZones = new ArrayList<>(zones);
                    int randomNumber = ThreadLocalRandom.current().nextInt(0, newZones.size());
                    List<Node> nodesFromRandomZone = this.zoneIdToNodes.get(newZones.get(randomNumber));
                    System.out.println("DEBUG : "+nodesFromRandomZone.toString());
                    Collections.shuffle(nodesFromRandomZone);
                    for(Node n : nodesFromRandomZone)
                    {
                        if(!n.getIsSpunDown() && !primaryNodes.contains(n))
                        {
                            currentHandOffNodes.add(n);
                            break;
                        }
                    }
                }
                else
                {
                    System.out.println("Cannot Assign HandOff nodes : Need to Spin UP some disks");
                }
                tries++;
            }
            if(tries == 1000)
            {
                System.out.println("Could Not Assign the HandOff Nodes");
                System.exit(999);
            }
        }
        result.put("primary", primaryNodes);
        result.put("handOff", currentHandOffNodes);
        return result;
    }

    public List<Node> getInactiveNodes(String filePath)
    {
        List<Node> inactiveNodes = new ArrayList<>();
        for(Node n : getNodes(filePath))
        {
            if(n.getIsSpunDown())
            {
                inactiveNodes.add(n);
            }
        }
        return inactiveNodes;
    }

    public ArrayList<Node> getAllNodes()
    {
        return new ArrayList<>(nodes.values());
    }

    public static Ring buildRing(HashMap<Integer,Node> nodes, int partitionPower, int replicas)
    {
        ArrayList<Integer> partitionToNode = new ArrayList<Integer>();
        long parts = (long) 2 << partitionPower;
        double totalWeight = 0.0;
        for(Node obj : nodes.values())
        {
            totalWeight += obj.getWeight();
        }
        for(Node obj : nodes.values())
        {
            obj.setDesiredParts(parts / totalWeight * obj.getWeight());
        }
        for(int part=0; part<parts; part++)
        {
            Boolean isNotBreak = true;
            for(Node obj : nodes.values())
            {
                if(obj.getDesiredParts() >= 1)
                {
                    obj.decrementDesiredParts();
                    partitionToNode.add(obj.getID());
                    isNotBreak = false;
                    break;
                }
            }
            if(isNotBreak)
            {
                for(Node obj : nodes.values())
                {
                    if(obj.getDesiredParts() >= 0)
                    {
                        obj.decrementDesiredParts();
                        partitionToNode.add(obj.getID());
                        break;
                    }
                }
            }
        }
        Collections.shuffle(partitionToNode);
        Ring ring = new Ring(nodes, partitionToNode, replicas);
        return ring;
    }

    public static void main(String[] args)
    {
        File file = new File("modules/cloudsim/src/main/java/org/cloudbus/cloudsimdisk/examples/rings.in");
        try (BufferedReader in = new BufferedReader(new FileReader(file)))
        {
            int Node_Count, Partition_Power, Replicas;
            String filePath = in.readLine().trim();
            String a[] = in.readLine().trim().split(" ");
            Node_Count = Integer.parseInt(a[0]);
            Partition_Power = Integer.parseInt(a[1]);
            Replicas = Integer.parseInt(a[2]);
            HashMap<Integer, Node> hm = new HashMap<Integer, Node>();
            for(int i=0; i<Node_Count; i++)
            {
                a = in.readLine().trim().split(" ");
                int id = Integer.parseInt(a[0]);
                int zone = Integer.parseInt(a[1]);
                double weight = Double.parseDouble(a[2]);
                StorageModelHdd hddModel = MyConstants.STORAGE_MODEL_HDD; // model of disks in the persistent storage
                PowerModelHdd hddPowerModel = MyConstants.STORAGE_POWER_MODEL_HDD; // power model of disks

                hm.put(id, new Node(id, zone, weight, hddModel, hddPowerModel));
            }
            Ring ring = buildRing(hm, Partition_Power, Replicas);
            for(Node n : ring.getNodes(filePath))
            {
                System.out.println(n);
            }
            System.out.print("**\n");
            Map<String, List<Node>> activeNodes = ring.getActiveNodes(filePath);
            for(String str : activeNodes.keySet())
            {
                System.out.println(str);
                for(Node n : activeNodes.get(str))
                {
                    System.out.println(n);
                }
                System.out.println();
            }
        }
        catch (Exception e)
        {
            new Exception("Main");
        }
    }
}
