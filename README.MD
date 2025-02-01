Very lightweight webserver

How can I start?
```
java -Dfile.encoding=UTF-8 -jar aurelius.jar
```

Sample project directory
```
settings.yml
aurelius.java
start.bat
app 
  - login
     - page.html
     - page.css
     - page.js
  - info
     - page.html
     - page.css
     - page.js
  main.html
  main.css
  main.js
```
main.html main directory is "/". All folder names under the app folder represent a root.

All frameworks and plugins supported:
- Tailwind CSS and more css frameworks..

Check out the releases for a sample application.

Soon to be added (Coming soon):
- Sending img etc files (%2)
- Restful service publishing
- Send JSON
- Mini IOC Container

