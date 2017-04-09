import json
import os.path,subprocess
from subprocess import STDOUT,PIPE
from flask import Flask, request
app = Flask(__name__)

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
	output = execute_java()
	return 'Output : '+output


@app.route('/start_simulation', methods=['POST'])
def start_simulation():
	data = request.get_json()
	filepath = "/Users/skulkarni9/Desktop/8thSem/GSS/server/data/input_data.json"
	with open(filepath, "w") as f:
		f.write(str(data))

	args = [str(x) for x in data.values()]
	print("Request : "+str(args))

	output = execute_java(args)
	return 'Output : '+output

if __name__ == '__main__':
	app.run(
		host="127.0.0.1",
		port=int("5000")
	)