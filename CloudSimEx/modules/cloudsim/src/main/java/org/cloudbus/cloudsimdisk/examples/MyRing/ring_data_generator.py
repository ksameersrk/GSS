import random
def gen_data(no_of_nodes):
    n = 0
    for region in range(0,4):
        for zone in range(0,4):
            for node in range(0, no_of_nodes//16):
                print "Region"+str(region)+",Zone"+str(zone)+",Node"+str(n)+","+str(round(random.uniform(0.5,1.0), 2))
                n=n+1

gen_data(64)