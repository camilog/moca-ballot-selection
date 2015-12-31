# BallotSelection
Third part of the [*MoCa QR*](http://mocaqr.niclabs.cl) Voting System project.

Android app where the voter makes her selections, then the app encrypts them, next the voter signs this encryption with her [VoterApp](https://github.com/niclabs/moca-voter-app) and finally the ballot is printed.

## Files
1. **DisplayCandidatesActivity.java**:

2. **BallotConfirmationActivity.java**:

3. **ShowEncryptedBallotQRActivity.java**:

4. **GenerateQRCodeActivity.java**:

### Minimum Requirements
### Hardware

### Apps installed

## How to Use
* Make sure you satisfy the minimum requirements described above.
* Install the .apk file, which can be downloaded from [here](https://github.com/CamiloG/moca_qr/blob/master/Precinct_Apps/ballotSelection.apk?raw=true).

### Configuration
* Make sure that you have access to the files of the public key and the list of candidates to configure this application.
* Select 'Configure Public Information'.
* Choose the public key file (.../publicKeyN.key).
* Next, choose the candidates file (.../candidates.xml).
* A remainder in the top of the main window shows that we had already configure these necessary files.

### Voting Process
* The voter first of all, makes her selection of the candidate that wants to vote for.
* Then, the app ask for a confirmation for the selection the voter did.
* Next, the app (in background) encrypts that previous selection using the public key of the authority.
* Then, is necessary that the voter signs the encryption, using VoterApp.
* After signing, the app generates the ballot (plain text, encryption and randomness).
* Automatically, the app prints the ballot, and then the process finishes, waiting for the next voter.
