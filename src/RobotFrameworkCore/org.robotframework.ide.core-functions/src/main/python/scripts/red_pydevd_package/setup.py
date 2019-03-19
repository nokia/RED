from setuptools import setup, find_packages
from redpydevd import __version__

setup(
    name='red-pydevd',
    version=__version__,
    author='Nokia Solutions and Networks',
    author_email='UNKNOWN',
    description='RED Robot Editor and PyDev debugger runner',
    long_description=open('README.md').read(),
    url='https://github.com/nokia/RED',
    packages=find_packages(),
    classifiers=[
        'Programming Language :: Python :: 2',
        'Programming Language :: Python :: 3',
        'License :: OSI Approved :: Apache Software License',
        'Operating System :: OS Independent',
    ],
)