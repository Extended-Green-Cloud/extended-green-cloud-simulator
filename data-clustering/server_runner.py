from flask import Flask
from dotenv import load_dotenv
from os import getenv
from src.rest.clustering_rest import clustering_bp

load_dotenv()
app = Flask(__name__)

def run_application():
    port = getenv('REST_PORT')
    host = getenv('REST_HOST')

    app.register_blueprint(clustering_bp, url_prefix='/clustering')
    app.run(host=host, port=port, debug=True)

if __name__ == '__main__':
    run_application()