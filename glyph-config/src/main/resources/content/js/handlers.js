// _data store handler

var dataHandler = editor.createHandler("data");

dataHandler.prototype.set = function (el, data) {
    this.data = data;

    document.getElementById("serverName").innerText = data.info.name;
    document.getElementById("serverIcon").src = data.info.icon;
};

dataHandler.prototype.get = function () {
    return this.data || {info: {name: "Broken Config", icon: "img/default-icon.png"}};
};

// Checkbox handler

var checkboxHandler = editor.createHandler("checkbox");

checkboxHandler.prototype.set = function (el, value) {
    el.checked = value;
};

checkboxHandler.prototype.get = function (el) {
    return el.checked;
};

// Wiki sources parser

var wikiSourcesParser = editor.createHandler("wikiSources");

wikiSourcesParser.prototype.set = function (el, value) {
    if (el.choices === undefined) {
        el.choices = new Choices(el, {
            addItemFilterFn: function (value) {
                return (value.split(" ").length === 1);
            }
        });
    }

    el.choices.clearStore();

    el.choices.setValue(value);
};

wikiSourcesParser.prototype.get = function (el) {
    if (el.choices === undefined) return [];

    return el.choices.getValue(true);
};

// Selectable roles parser

var selectableRolesParser = editor.createHandler("selectableRoles");

selectableRolesParser.prototype.set = function (el, value, data) {
    if (el.choices === undefined) {
        el.choices = new Choices(el);
    }

    el.choices.clearStore();

    var choices = data.roles.map(function (role) {
        return {
            value: role.id,
            label: role.name,
            disabled: !role.canInteract,
            selected: value.includes(role.id)
        };
    });

    el.choices.setChoices(choices, "value", "label", true);
};

selectableRolesParser.prototype.get = function (el) {
    if (el.choices === undefined) return [];

    return el.choices.getValue(true).map(function (value) {
        return value;
    });
};

// Channel selection parser

var channelSelectionParser = editor.createHandler("channel");

channelSelectionParser.prototype.set = function (el, value, data) {
    if (el.choices === undefined) {
        el.choices = new Choices(el);
    }

    el.choices.clearStore();

    var choices = data.textChannels.map(function (channel) {
        return {value: channel.id, label: channel.name, selected: (value === channel.id)};
    });

    el.choices.setChoices(choices, "value", "label", true);
};

channelSelectionParser.prototype.get = function (el) {
    if (el.choices === undefined) return [];

    return el.choices.getValue(true);
};

// Emoji selection parser

var emojiSelectionParser = editor.createHandler("emoji");

emojiSelectionParser.prototype.set = function (el, value, data) {
    if (el.choices === undefined) {
        el.choices = new Choices(el);
    }

    el.choices.clearStore();

    var serverEmoji = data.emojis.map(function (e) {
        var name = e.name;
        var shortname = "<span>:" + name + ":</span>";
        var img = "<img src='" + e.image + "' alt='" + name + "' />";
        var label = "<div class='emoji-selection'>" + img + shortname + "</div>";

        return {value: name, label: label, selected: (value === name)};
    });
    var allEmoji = Object.entries(emoji).map(function (e) {
        var name = e[0];
        var shortname = "<span>:" + name + ":</span>";
        var img = twemoji.parse(e[1]);
        var label = "<div class='emoji-selection'>" + img + shortname + "</div>";

        return {value: name, label: label, selected: (value === name)};
    }).concat(serverEmoji);

    el.choices.setChoices(allEmoji, "value", "label", true);
};

emojiSelectionParser.prototype.get = function (el) {
    if (el.choices === undefined) return [];

    return el.choices.getValue(true);
};
