# BallotSelection
Third part of the [*MoCa QR*](https://github.com/CamiloG/moca_qr) Voting System project.

Android app where the voter make her selections, then the app encrypts it, next the voter sign this encryption with her app [SignatureApp](https://github.com/CamiloG/SignatureApp) and finally the ballot is printed.

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
* At the first execution of the app, it needs to be configured with the address of the Bulletin Board server, so the app can download the candidates file and the public key from the authority.

### Voting Process
* The voter first of all, make her selection of the candidate that want to vote for.
* Then, the app ask for a confirmation for the selection the voter did.
* Next, the app (in background) encrypts that previous selection using the public key of the authority.
* Then, is necessary that the voter signs the encryption, using the SignatureApp.
* After signing, the app generates the ballot (plain text, encryption and randomness).
* Automatically, the app prints the ballot, and then the process finishes, waiting for the next voter.

