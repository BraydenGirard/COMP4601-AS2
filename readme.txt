Assignment #2 - COMP 4601

By: Benjamin Sweett & Brayden Girard
    100846396         100852106

Known Bugs:

  - Server will throw and exception if database is empty (no crawl data)

  - When server is reset the crawler must be manually run and then
    server must be restarted

Testing:

  - Before running anything, a directory needs to be created in
    ~/data/lucene/index-directory this allows the lucene to save indexes

  - Start Mongo database

  - Run the class edu.carleton.comp4601.assignment2.crawler.Controller
    as a Java application and this will crawl the internet

  - Once crawler terminates, right click the project and run as server
    this will run the web server

  - To test the restful web service run the client in xcode and make
    requests that match those in requirement 8 of assignment

  - Alternate method of testing restful web service is to open browser
    and navigate to http://localhost:8080/COMP4601A2-100852106/rest/sda/cmd
    where cmd is the path from requirement 8

  - When /reset is called, you must then stop the web server and restart
    from step 3 of testing
