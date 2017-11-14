# SensorFlow
A pure Java library for the unified sensor-data streams management.
# SensorFlow
A framework for the unified sensor-data streams management.
The framework is composed of two blocks:
1. Real-life sensing
2. Data management

**Issues**
1. Heterogeneity: of devices, protocols, cloud solutions and applications
2. Synchronization
3. Security/privacy

**Key paradigm**: Modularity and common interfaces

**Components**:
* BSN Manager (BSNM)
* Input Module
* Output Module
* Processing Module
* User Interface

**Data types**:
* Signals: Series of (timestamp,value) pairs conveying the information from the sensors
* Metadata: Information about the composition of the BSN and characteristics of the collected signals
* Messages: Series of (timestamp, {event}) pairs that describe the status of the network with possible errors. Used by the modules to communicate their current status to the BSNM.

## Components
### BSNM
Manages the BSN through the connected modules.
Main functions:
* Set the BSN and provide a time reference to the input plugins for the synchronization (together with latency estimation)
* Check the status of the BSN
* Start and stop the acquisition

### Input Module
Manages the input protocols and exposes a common interface to the BSNM.
Two distinct functions:
* Interfaces with the device
* Manages the data streams

### Output Module
Manages the output protocols and exposes a common interface to the BSNM according to the application
Two distinct functions:
* Interfaces with the destination
* Manages the security/privacy

### Processing Module
Provides real-time capabilities
Two types:
* (quasi) Real-Time
* Post-acquisition

### User Interface
Exploits the BSNM to allow the user to set the BSN

## Development plan
### API::DJango Rest Server
* DJango admin [Done]
* Authenticate users via token [Done]
* Provide profile info [Done]
* Provide tokens to crossbar for auth
* Browse uploads
* User MRUs
* Browse experiments and sessions
* Browse plugins

### Researcher::WebGUI
* Data management & download
* Session management: experiments and if they have real time feedback
* Plugin management
* Account management

### Experimenter::Mobile
* Authenticates [Done]
* Uploads data (September)
* Receives feedback

### Plugin::Remote Client
* Authenticates
* Receives data through WebSocket/MQTT

### Database::MongoDB Server
* Design schema [Done]
* Define data ("Collection")
