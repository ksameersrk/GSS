file_dict = dict()
size = 0.0

with open("gss_workload_case1_4.txt") as f:
    for line in f:
        data = line.strip().split(",")
        if data[0] == "PUT" or data[0] == "UPDATE":
            file_dict[data[2]] = data[3]

bigger_than_10MB = 0
for k,v in file_dict.items() :
    size = size + float(v)
    if float(v) >= 10.0 :
        bigger_than_10MB = bigger_than_10MB + 1
    print (k + " : " + str(v))

print ("total size sum : " + str(size))
print ("total no. of files : " + str(len(file_dict.keys())))
print ("total no. of files bigger than 10MB : " + str(bigger_than_10MB))
