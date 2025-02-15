# Very lightweight webserver

## How can I start?
```
java -Dfile.encoding=UTF-8 -jar aurelius.jar
```

## All frameworks and plugins supported:
- Tailwind CSS and more css frameworks..

## Placeholder usage:
### placeholders.yml:
```
title: "Aurelius"
```
### main.html
```
<!DOCTYPE html>
<html>
<head>
<link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
</head>
<body>

<h1>%title% Example</h1>

</body>
</html>

```

## Info
Check out the releases for a sample application.
main.html main directory is "/". All folder names under the app folder represent a root.
You can sort your media files such as mp4, jpeg, png and yml, json under the "public" folder and use them on the site. Check out the test project!

## Soon to be added (Coming soon):
- Restful service publishing
- Mini IOC Container

## Sample project directory
```
settings.yml
aurelius.java
start.bat
placeholders.yml
public
   video1.mp4
   image1.png
   data1.json
   data2.yaml
app 
  - page1 # domoin.com/page1
     - page.html
     - page.css
     - page.js
  main.html
  main.css
  main.js
```
