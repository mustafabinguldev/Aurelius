# Very lightweight webserver


### Learn before you start
Check out the releases for a sample application.
main.html main directory is "/". All folder names under the app folder represent a root.
You can sort your media files such as mp4, jpeg, png and yml, json under the "public" folder and use them on the site. Check out the test project!
#### Example Project: [Go to project](https://github.com/mustafabinguldev/aurelius-example-project)



### All frameworks and plugins supported:
- Tailwind CSS, Bootstrap and more css frameworks..

### Container usage:
#### main.html:
```
<html>
<body>

<h1>Example Text</h1>
<container.button text="Example Button"></container.button>
<container.section></container.section>

</body>
</html>
```
#### containers/button.html
```
<button type="button" class="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800">
{text}</button>
```
#### containers/section.html
```
<p>Example Text!</p>
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

### Prerequisites

Before you begin, ensure that you have the following software installed on your local machine:

- **Java 8 or later**: Aurelius is built using Java, so you need to have Java installed on your system. To check if Java is installed, run the following command:

    ```bash
    java -version
    ```

  If Java is not installed, download and install it from the official [Java website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).

### Installation

Follow these steps to set up Aurelius on your machine:

- **1**. Download the latest version from Releases, download the jar file [Download](https://github.com/mustafabinguldev/Aurelius/releases)
- **2**. Put the jar in an empty folder.
- **3**. Create a bat file and paste the launch code and then launch it.
``` bash
java -Dfile.encoding=UTF-8 -jar aurelius.jar
```



