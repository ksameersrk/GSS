import sys
import random

ops = ["GET", "UPDATE", "DELETE"]

files = dict()
data = []
dataset = []

for i in range(1000):
	data.append("files"+str(i))
	dataset.append("PUT,"+str(random.randint(0, 250))+",files"+str(i)+","+str(random.randint(1, 10000)))

for i in range(1000):
	data.append("files"+str(i))
	dataset.append("GET,"+str(random.randint(250, 500))+",files"+str(i))

for i in range(1000):
	data.append("files"+str(i))
	dataset.append("UPDATE,"+str(random.randint(500, 750))+",files"+str(i)+","+str(random.randint(1, 10000)))

for i in range(1000):
	data.append("files"+str(i))
	dataset.append("DELETE,"+str(random.randint(750, 1000))+",files"+str(i))

with open("inputIdealDataset1000Ops.txt", "w") as f:
	f.write("\n".join(dataset))
