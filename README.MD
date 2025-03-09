# Very lightweight webserver

### Features
- **1.** Easy Setup: Simple installation process with Java 8 or later.
- **2.** Web Development Support: Supports Tailwind CSS, Bootstrap, and other CSS frameworks.
- **3.** Container System: Allows dynamic content generation using containers such as buttons and sections.
- **4.** Media Support: Handles various media files like mp4, jpeg, png, yml, and json through a public folder.
- **5.** Placeholder System: Supports placeholders like %title% in HTML files, which can be replaced dynamically.
- **6.** Configurable Server Settings: Allows configuration of server settings such as port and thread size via a settings.yml file.
- **7.** Built-in UI: Provides a user interface with an option for easy server management (start, stop, reload).
- **8.** Cross-platform: Works across all platforms with executable and JAR versions
- **9.** Speed and Simplicity: It provides convenience to the developer with speed and simplicity.

### Learn before you start
Check out the releases for a sample application.
main.html main directory is "/". All folder names under the app folder represent a root.
You can sort your media files such as mp4, jpeg, png and yml, json under the "public" folder and use them on the site. Check out the test project!
#### Example Project: [Go to project](https://github.com/mustafabinguldev/aurelius-example-project)

### Prerequisites

Before you begin, ensure that you have the following software installed on your local machine:

- **Java 8 or later**: Aurelius is built using Java, so you need to have Java installed on your system. To check if Java is installed, run the following command:

    ```bash
    java -version
    ```

  If Java is not installed, download and install it from the official [Java website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).

### Installation

Follow these steps to set up Aurelius on your machine:

**Windows**
- **1**  Download the latest version from Releases, download the exe file [Download](https://github.com/mustafabinguldev/Aurelius/releases)
- **2**  Put the jar in an empty folder.
- **3**  Launch the exe file and it's that simple!

**All Platforms**
- **1**. Download the latest version from Releases, download the jar file [Download](https://github.com/mustafabinguldev/Aurelius/releases)
- **2**. Put the jar in an empty folder.
- **3**. Create a bat file and paste the launch code and then launch it.
``` bash
java -Dfile.encoding=UTF-8 -jar aurelius.jar
```


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

### settings.yml
 ```
server:
   port: 8080
   threadSize: 2
   ui: true
 ```

### Addons & RESTful:
You can design the web server exactly as you want, either by installing other people's addons or by writing your own addon! Example Addon:
[Go to project](https://github.com/mustafabinguldev/AureliusExampleAddon)
#### Example:
```
     //domoin:8080/api/test POST
      AddonManager.
                registerRestFulService(new RestFulResponseStructure.Builder("test").
                        setRestFulResponse(new UserDtoController()).
                setRequestType(RestFulRequestType.POST).build());   
      //domoin:8080/api/hello GET
AddonManager.registerRestFulService(new RestFulResponseStructure.Builder("hello").
                        setRestFulResponse(new RestFulResponseStructure.RestFulResponse<String, Object>() {
                            @Override
                            public String response(Object o, RestFulResponseHelper restFulResponseHelper) {
                            
                                CookieStructure cookieStructure = new CookieStructure();
                                cookieStructure.setCookieName("randomid");
                                cookieStructure.setCookieValue(""+new Random().nextInt(10000));
                                cookieStructure.setFeatures(Arrays.asList(new CFMaxAge(3600), new CFHttpOnly()));
                                restFulResponseHelper.sendCookie(cookieStructure);
                                
                                return "Hello world ! Path data:"+ Arrays.toString(restFulResponseHelper.getPathData());
                            }

                            @Override
                            public Object convert(String s) throws Exception {
                                return null;
                            }
                        }).
                        setRequestType(RestFulRequestType.GET).
                        build());
```
```
import tech.bingulhan.dto.UserDto;
import tech.bingulhan.dto.request.UserRequest;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.app.addon.AddonManager;
public class UserDtoController implements RestFulResponseStructure.RestFulResponse<UserDto, UserRequest> {

    @Override
    public UserDto response(UserRequest request, RestFulResponseHelper restFulResponseHelpe) {

        CookieStructure cookieStructure = new CookieStructure();
        cookieStructure.setCookieName("randomid2");
        cookieStructure.setCookieValue(""+new Random().nextInt(10000));
        cookieStructure.setFeatures(Arrays.asList(new CFMaxAge(3600), new CFHttpOnly()));
        restFulResponseHelper.sendCookie(cookieStructure);
        
        UserDto userDto = new UserDto();
        userDto.setUsername(request.getUsername());
        userDto.setEmail(request.getEmail());

        return userDto;
    }

    @Override
    public UserRequest convert(String s) throws Exception {
        return (UserRequest) AddonManager.convertFromBodyJson(s, UserRequest.class);
    }
}
```

```
@Getter
@Setter
@ToString
public class UserRequest {
    private String username;
    private String email;
}
@Getter
@Setter
@ToString
public class UserDto {
    private String username;
    private String email;
}


```

