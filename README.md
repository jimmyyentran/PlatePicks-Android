# FoodTinder
## Compile & Running
The backend-API backbone of this project is generated using Amazon Web Service (AWS) Mobile Hub https://aws.amazon.com/mobile/. AWS Mobile Hub is a fairly new service (Late 2015) that allows developers to develop apps using mobile backend as a service (MBaaS). What this means is that as developers we do not need to waste time setting up and dealing with the nuances of running a server and configuring backend logic. AWS Mobile Hub does all of this for us, allowing for us to concentrate on making the actual app. However its services does not come free. For the first 12 months of usage, AWS has a "grace period" where it does not charge for limited use of services unless we exceed our memory, API call, analytics, etc usage. Because we don't plan to exceed 1,000 daily users after launching (who knows?), we should be fine using AWS for the entirity of this project. 

In the READ_ME directory of this repository is an `index.html` that will guide you through the steps of compiling the project on your local machine. You can skip the Facebook Sign-in (steps 5 & 6).

We will be using the sample app as a starter App (step 28). The name have already been changed but we'd need to remove demo features.
