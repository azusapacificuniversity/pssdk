"""
Flask application to interact with PeopleSoft Component Interfaces.
"""
import java
from flask import Flask, jsonify

app = Flask(__name__)

AppServer = java.type('edu.apu.pssdk.AppServer')
appServer = AppServer.fromEnv()


def component_interface(ci_name: str, opts: dict = None):
    """
    Retrieves a component interface from the application server.

    Args:
        ci_name (str): The name of the component interface to retrieve.
        opts (dict, optional): Options to pass to the CI factory.

    Returns:
        object: An instance of the requested component interface.
    """
    if opts is None:
        opts = {}
    return appServer.ciFactory(ci_name, opts)


@app.route('/api/v1/profiles/<string:userid>')
def get_user_profile(userid: str):
    """
    Returns the Peoplesoft user profile for the user ID as JSON.

    Args:
        userid (str): The OPRID of the user.

    Returns:
        dict: A dictionary containing the user profile data.
    """
    profile = component_interface("USER_PROFILE").get({"UserID": userid})
    return jsonify(profile)


@app.route('/')
def root():
    """
    Handles requests to the root URL.
    """
    return 'root'

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
