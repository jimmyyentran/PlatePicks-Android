<p align="center"><img src="/app/src/main/res/drawable/main_logo.png" width="300"></p>

[![Build Status](https://travis-ci.com/jtran064/PlatePicks.svg?token=knkpqxx7mmwUCSBzx5mB&branch=master)](https://travis-ci.com/jtran064/PlatePicks)  
# Plate Picks

Tinder but for food

## Progress

### General
&#x2705; App name  
&#x2705; App logo  
&#x2705; User can make app-specific account  
&#x2705; User can save liked foods onto online database  
&#x2705; User can leave food reviews  
&#x2705; User can login from Facebook  

### Front End

&#x2705; Splash screens does preliminary loading of screens and database requests  
&#x2705; Fully working list of liked food  
&#x2705; Users can swipe left to view next item  
&#x2705; Working sliding animations  
&#x2705; Notification within app that item is added to list  
&#x2705; Distinctive visual separation between viewed and unviewed items in list  
&#x2705; Simple splash screen

### Back End
&#x2705; Ethnic food searches  
&#x2705; Load (50) food images onto AWS S3 with name and restaurants  
&#x2705; Working schemas for users, food, and restaurants and their relationships  
&#x2705; Working Yelp API integrated into app  
&#x2705; RESTful API  
&nbsp;&nbsp;&nbsp;&nbsp;&#x2705; GET - pull image from AWS S3  
&nbsp;&nbsp;&nbsp;&nbsp;&#x2705; CINSERT - append liked foods into DynamoDB  
&nbsp;&nbsp;&nbsp;&nbsp;&#x2705; DELETE - remove liked food from list  
&#x2705;Range specific searches

## Tools

* Amazon Web Service
  * Cognito
  * DynamoDB
  * Lambda
  * Mobile Hub
  * S3
* Android Studios
* Gson
* Robolectric

## Squad

Eliza Alcaraz  
Divyansh Choudhary  
Daniel Handojo  
Alyza Malunao   
Jordan Kincer   
Jimmy Tran

## License

MIT
