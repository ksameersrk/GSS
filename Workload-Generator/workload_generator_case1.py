all_lines = open("f_changed_campus1_4.txt", "r").readlines()
filename_size_map = {}

f_output = open("gss_workload_case1_4.txt", "w")
index = 0
CONVERT_TO_MB = 1048576.0;
SEED_TIME = 30964;
TO_SECONDS = 1000;
for line in all_lines:
    ops_list = line.split(" ")
    timestamp = str((float(ops_list[1])-30964)/TO_SECONDS)
    filename = "file"+ops_list[7]
    filesize = float(ops_list[11]) / CONVERT_TO_MB
    operation = ops_list[12]

    if operation == "up":
        if filename in filename_size_map.keys():
            filename_size_map[filename] = filename_size_map.get(filename) + filesize
            line = "UPDATE," + timestamp + "," + filename + "," + str(filename_size_map.get(filename))
        else:
            filename_size_map[filename] = filesize
            line = "PUT," + timestamp + "," + filename + "," + str(filename_size_map.get(filename))

    else:
        line = "GET," + timestamp + "," + filename

    f_output.write(line+"\n")

    index += 1
    if(index % 100 == 0):
        print(index)