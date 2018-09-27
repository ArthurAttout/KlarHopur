from flask import render_template, make_response, request, jsonify, send_file
from flask import Flask
import string
import json
import random
import base64
import os

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False



@app.errorhandler(404)
def not_found(error):
    resp = make_response(error, 403)
    resp.headers['Error'] = error
    return resp

# upload a picture and return the link for her


@app.route("/upload", methods=["POST"])
def upload():
    randomisation = ''.join(random.choice(
        string.ascii_uppercase + string.digits) for _ in range(9))
    randomisation += ".png"
    image = open(randomisation, "wb")
    image.write(base64.b64decode(request.get_data()))
    image.close()
    command = "mv {} images/".format(randomisation)
    os.popen(command)
    rep = "https://prom.longree.be/image/{}".format(randomisation)
    ret = {'URL': rep}
    ret = jsonify(ret)
    print(ret)
    response = make_response(ret, 200, {'Content-Type': "application/json"})
    print("make_reponse")
    print(response)
    return response
# getter for images


@app.route("/image/<name>", methods=["GET"])
def image(name):
    print(name)
    path = "images/{}".format(name)
    return send_file(path, mimetype='image/png')

# main parameter of Flask Application
if __name__ == "__main__":
    app.run(host='0.0.0.0')
