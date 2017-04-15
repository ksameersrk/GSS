import json
import os.path,subprocess
from subprocess import STDOUT,PIPE
from flask import Flask, request
from flask.ext.cors import CORS, cross_origin
app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'


def execute_java(args):
	stdin=PIPE
	compile_comand = open("command.txt", "r").readline().strip()
	#print(compile_comand)
	cmd2 = compile_comand.strip().split(" ")+args
	proc = subprocess.Popen(cmd2, stdin=PIPE, stdout=PIPE, stderr=STDOUT, cwd='/Users/skulkarni9/Desktop/8thSem/GSS/CloudSimEx')
	stdout,stderr = proc.communicate()
	return stdout.decode("utf-8")

@app.route('/')
def hello_world():
	return 'Hello!'

@app.route('/get_large_datasets', methods=['GET'])
def large_datasets():
	result = {}
	rootJson = "/Users/skulkarni9/Desktop/8thSem/GSS/server/data/large_datasets/large_datasets.json"
	rootPath = "/Users/skulkarni9/Desktop/8thSem/GSS/server/data/large_datasets/"

	with open(rootJson) as f:
		result["tableData"] = json.load(f)
		for entry in result["tableData"]:
			id_ = entry["id"]
			piePath = rootPath+id_+"/pie_chart.json"
			linePath = rootPath+id_+"/line_chart.json"
			logsPath = rootPath+id_+"/output_logs.txt"

			entry["logs"] = logsPath

			with open(piePath) as f:
				entry['pieChart'] = json.load(f)

			with open(linePath) as f:
				entry['lineChart'] = json.load(f)

	return json.dumps(result)


@app.route('/start_simulation', methods=['POST'])
@cross_origin()
def start_simulation():
	data = request.get_json()
	rootPath = '/Users/skulkarni9/Desktop/8thSem/GSS/server/data/'
	filepath = rootPath+'input_data.json'
	with open(filepath, "w") as f:
		f.write(str(data))

	print("required scenario :::::::::::  "+str(data['scenario']))

	request_scenario = int(str(data['scenario']))
	args = [str(x) for x in data.values()]
	print("Request : "+str(args))

	logs = execute_java(args)
	result = {};
	with open("/Users/skulkarni9/Desktop/8thSem/GSS/server/data/output_logs.txt", "w") as f:
		f.write(logs)

	result['logs'] = '/Users/skulkarni9/Desktop/8thSem/GSS/server/data/output_logs.txt'

	'''
	line_chart_active_energy.json
	line_chart_active_time.json
	line_chart_idle_energy.json
	line_chart_idle_time.json
	line_chart_total_power.json
	output_logs.txt
	pieChartActiveVsSpundownWithStagingDisk.json
	pieChartActiveVsSpundownWithoutStagingDisk.json
	pieChartWithVsWithoutSSD.json
	'''

	lc_ae_path = rootPath+'line_chart_active_energy.json'
	lc_at_path = rootPath+'line_chart_active_time.json'
	lc_ie_path = rootPath+'line_chart_idle_energy.json'
	lc_it_path = rootPath+'line_chart_idle_time.json'
	lc_tp_path = rootPath+'line_chart_total_power.json'
	pie_with_path = rootPath+'pieChartActiveVsSpundownWithStagingDisk.json'
	pie_without_path = rootPath+'pieChartActiveVsSpundownWithoutStagingDisk.json'
	pie_both_path = rootPath+'pieChartWithVsWithoutSSD.json'

	with open(lc_ae_path) as f:
		result['lc_ae_path'] = json.load(f)

	with open(lc_at_path) as f:
		result['lc_at_path'] = json.load(f)

	with open(lc_ie_path) as f:
		result['lc_ie_path'] = json.load(f)

	with open(lc_it_path) as f:
		result['lc_it_path'] = json.load(f)

	with open(lc_tp_path) as f:
		result['lc_tp_path'] = json.load(f)

	if request_scenario == 1:
		with open(pie_without_path) as f:
			result['pie_without_path'] = json.load(f)
	elif request_scenario == 2:
		with open(pie_with_path) as f:
			result['pie_with_path'] = json.load(f)
	elif request_scenario == 3:
		with open(pie_without_path) as f:
			result['pie_without_path'] = json.load(f)
		with open(pie_with_path) as f:
			result['pie_with_path'] = json.load(f)
		with open(pie_both_path) as f:
			result['pie_both_path'] = json.load(f)

	

	'''
	piePath = '/Users/skulkarni9/Desktop/8thSem/GSS/server/data/pie_chart.json'
	linePath = '/Users/skulkarni9/Desktop/8thSem/GSS/server/data/line_chart.json'


	with open(piePath) as f:
		result['pieChart'] = json.load(f)
		

	with open(linePath) as f:
		result['lineChart'] = json.load(f)

	'''

	return json.dumps(result)

if __name__ == '__main__':
	app.run(
		host="127.0.0.1",
		port=int("5000")
	)