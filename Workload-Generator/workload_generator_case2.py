all_lines = open("gss_workload_case1_4.txt", "r").readlines()
f_output = open("gss_workload_case2_4.txt", "w")
#all_lines = open("sample_in.txt", "r").readlines()
#f_output = open("sample_out.txt", "w")
#gss_workload_case1_4
filename_ops_index_map = {}

for index in range(0, len(all_lines)):
    ops_list = all_lines[index].split(",")
    filename = ops_list[2].strip()
    operation = ops_list[0].strip()

    if filename in filename_ops_index_map.keys():
        if operation == "UPDATE":
            filename_ops_index_map[filename].append([index, False])
        elif operation == "GET":
            filename_ops_index_map[filename][-1][-1] = True
    else:
        if operation == "UPDATE":
            filename_ops_index_map[filename] = [[index, False]]

print("At the end of mapping")

remove_nums_set = set()
for v in filename_ops_index_map.values():
    for i in range(len(v)-1):
        if not v[i][1]:
            remove_nums_set.add(v[i][0])

print("At the end of remove index set")

required_lines = set([x for x in range(len(all_lines))]) - remove_nums_set

print("Before printing")
for index in sorted(required_lines):
    f_output.write(all_lines[index])