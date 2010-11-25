function getWindowHeight() {
    if (window.self && self.innerHeight) {
        return self.innerHeight;
    }

    if (document.documentElement && document.documentElement.clientHeight) {
        return document.documentElement.clientHeight;
    }

    return 0;
}


function resizeMap()
{
    var offset = 0;
    var windowHeight = getWindowHeight();

    for (var elem = document.getElementById("map"); elem != null; elem = elem.offsetParent) {
        offset += elem.offsetTop;
    }

    // 15px off the bottom
    var height = windowHeight - offset -15;

    if (height >= 0) {

        if(document.getElementById("map"))
         document.getElementById("map").style.height = height + "px";

        if(document.getElementById("info"))
         document.getElementById("info").style.height = height + "px";
    }

}

