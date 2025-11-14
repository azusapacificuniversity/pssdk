"""
FastAPI application to interact with PeopleSoft Component Interfaces.
"""
import java
from fastapi import FastAPI

app = FastAPI()

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


@app.get('/api/v1/profiles/{userid}')
def get_user_profile(userid: str):
    """
    Returns the Peoplesoft user profile for the user ID as JSON.

    Args:
        userid (str): The OPRID of the user.

    Returns:
        dict: A dictionary containing the user profile data.
    """
    return dict(component_interface("USER_PROFILE")
                .get({"UserID": userid}))


@app.get('/')
def root():
    """
    Handles requests to the root URL.
    """
    return 'root'
