# Very lightweight webserver


### Example Project
#### [Go to project](https://github.com/mustafabinguldev/aurelius-example-project)


### All frameworks and plugins supported:
- Tailwind CSS, Bootstrap and more css frameworks..

### Container usage:
#### main.html:
```
<html>
<body>

<h1>Example Text</h1>
<container.button></container.button>

</body>
</html>
```
#### containers/button.html
```
<button type="button" class="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800">
Example Button</button>
```

### Placeholder usage:
#### placeholders.yml:
```
title: "Aurelius"
```
#### main.html
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

### Media Support:
#### main.html
```
<img src="/public/example.png" alt="Example image" />
```
#### folder:
``` 
public
   example.png
```

### Learn before you start
Check out the releases for a sample application.
main.html main directory is "/". All folder names under the app folder represent a root.
You can sort your media files such as mp4, jpeg, png and yml, json under the "public" folder and use them on the site. Check out the test project!

### Soon to be added (Coming soon):
- Restful service publishing
- Mini IOC Container

### Sample project directory
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
  - page1 # host:port/page1
     - page.html
     - page.css
     - page.js
  main.html
  main.css
  main.js
```
