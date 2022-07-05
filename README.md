# javaEmailSendScreen

Full email screen, for SENDing emails (not receiving), built in JAVA / SPRING BOOT / JSF / PRIMEFACES.

I tried to build a complete module (front end / back end), so that other developers can attach it to their project, as long as this project is built with the same technologies.

As the objective here is not “to teach programming”, but to help other developers, I will not explain details, ok? I'll give you basic instructions, so you can get it all working.


<h3>project shape</h3>
The spring boot documentation recommends using HTML, with Thymeleaf to communicate with the back end. However, as I like to improve productivity, I prefer to work with JSF/PRIMEFACES, which has A LOT of ready-made and VERY functional components. However, for spring to work with JSF, some additional configurations are needed (you will find them throughout the code), starting with the arrangement of packages and folders. Do not change the locations of static elements and xhtml files as this will break the project.


<h3>applications.properties</h3>
Start by entering your email configuration information. That is, host, port, username, password, etc. Check your email provider's documentation (google, outlook, etc) and fill it in correctly. Otherwise, there will be connection errors and emails will not be sent.
You will also be able to modify the subscription data, in the email's footer: name, title, telephone number, etc.


<h3>database / flyway</h3>
I used Postgresql. You can use this same database, filling in your information in application.properties or use another db. If you decide to use the db, you will need to change the application.properties settings, the dependencies in the pom.xml, and MAYBE a few lines in the flyway sql files in the src/main/resources/db/migration folder.


<h3>fun facts</h3>
1) Whenever you send an email, the system will automatically send the same email to your own account. This is to compensate for the fact that emails are not received and attachments are not stored. They will be stored at your email provider. <h5>Contributions to add RECEIVING and STORING messages are welcome.</h5>
2) For each email sent, a record will be added to the postgresql database. The basic information of the email will be saved: from, to, cc, cc, attachment names (strings, not Files), body and sending date/time. You may use this information however you wish on your systems;
3) The signature, in the footer of the email, can be modified (only for the current email) by clicking on the button below it;
4) You can choose to open the email screen, with some fields already filled in, or open it blank. See the start.java file;
5) The to, Cc and Bcc fields are of the “autocomplete” type. Emails can be added using the “Add if not on the list” button. After that, in the next messages, the added email will appear in the autocomplete;
6) Attached files are deleted from your computer/server at the time the email is sent.
