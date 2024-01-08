/**
 * Displays hints in the save bar to help explain things
 * @param dataTag the data tag to select
 * @param displayID the element id for where to show the hint
 * @constructor
 */
function Hinter(dataTag, displayID) {
    this.hinted = document.querySelectorAll("[data-" + dataTag + "]");
    this.hintDisplay = document.getElementById(displayID);

    var myself = this;

    this.hinted.forEach(function (el) {
        el.onmouseover = function () {
            myself.hint(this.getAttribute("data-" + dataTag));
        };

        el.onmouseleave = function () {
            myself.hint("");
        };
    });
}

/**
 * Display a hint
 * @param message the message to display
 */
Hinter.prototype.hint = function (message) {
    this.hintDisplay.innerText = message;
};
