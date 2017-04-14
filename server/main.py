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


@app.route('/start_simulation', methods=['POST'])
@cross_origin()
def start_simulation():
	data = request.get_json()
	filepath = "/Users/skulkarni9/Desktop/8thSem/GSS/server/data/input_data.json"
	with open(filepath, "w") as f:
		f.write(str(data))

	args = [str(x) for x in data.values()]
	print("Request : "+str(args))

	logs = execute_java(args)

	piePath = '/Users/skulkarni9/Desktop/8thSem/GSS/server/data/pie_chart.json';
	linePath = '/Users/skulkarni9/Desktop/8thSem/GSS/server/data/line_chart.json';


	result = {};
	with open("/Users/skulkarni9/Desktop/8thSem/GSS/server/data/output_logs.txt", "w") as f:
		f.write(logs)

	result['logs'] = '/Users/skulkarni9/Desktop/8thSem/GSS/server/data/output_logs.txt'
	print("Logs came in")


	with open(piePath) as f:
		result['pieChart'] = json.load(f)
		

	with open(linePath) as f:
		result['lineChart'] = json.load(f)

	return json.dumps(result)

if __name__ == '__main__':
	app.run(
		host="127.0.0.1",
		port=int("5000")
	)