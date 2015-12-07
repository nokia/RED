# RED - Robot Editor
## General information 

RED is modern editor based on Java IDEs (Eclipse, IntelliJ in future) to allow quick and comfortable work with Robot testware. 

<script type="text/javascript">
    $(document).ready(function () {
        GetLatestReleaseInfo();
    });

    function GetLatestReleaseInfo() {
        $.getJSON("https://api.github.com/repos/Nokia/RED/releases/latest").done(function (release) {
            var asset = release.assets[0];
            var downloadCount = 0;
            for (var i = 0; i < release.assets.length; i++) {
                downloadCount += release.assets[i].download_count;
            }
            var oneHour = 60 * 60 * 1000;
            var oneDay = 24 * oneHour;
            var dateDiff = new Date() - new Date(asset.updated_at);
            var timeAgo;
            if (dateDiff < oneDay)
            {
                timeAgo = (dateDiff / oneHour).toFixed(1) + " hours ago";
            }
            else
            {
                timeAgo = (dateDiff / oneDay).toFixed(1) + " days ago";
            }
            var releaseInfo = release.name + " was updated " + timeAgo + " and downloaded " + downloadCount.toLocaleString() + " times.";
            $(".sharex-download").attr("href", asset.browser_download_url);
            $(".release-info").text(releaseInfo);
            $(".release-info").fadeIn("slow");
        });
    }
</script>

## What RED have 
* text editor with validation and code colouring
* table editors like in Ride (currently read-only)
* debug&remote debug
	* breakpoints
	* testcase stepping (step into, step over)
	* runtime variable lookup & modification
* code assistance & completion
* real time testcase validation
* execution view
* support for plugins via Eclipse mechanisms

## Look & feel
![](https://github.com/nokia/RED/blob/master/doc/img/red_overview_source_1.png "Robot perspective with text editor")

![](https://github.com/nokia/RED/blob/master/doc/img/red_testcases_table.png "Table editor")

![](https://github.com/nokia/RED/blob/master/doc/img/red_overview_debug.png "Debug perspective")


## Binaries distribution
RED is distributed as Eclipse feature to be installed on existing Eclipse platform. 


