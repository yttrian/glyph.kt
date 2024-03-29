Privacy Policy
==============

We understand that when you use a bot, there's a certain level of privacy you expect.
That is why we provide this information below, to clear up any concerns.

Opt-ins and opt-outs
--------------------

Within the Discord client, Glyph provides tools to manage your data usage with the :code:`/compliance` command.

In order to use the natural language processing features of Glyph, you must explicitly opt-in to allow your message
content to be sent to Dialogflow for processing. Read more below under `message contents`_.

Data we collect
---------------

session ids
^^^^^^^^^^^
:What: An `MD5`_ hash of your user id and the channel id
:When: Glyph is explicitly mentioned via DM or @mention
:Where: DialogFlow requests and message logs
:Why: Used with DialogFlow to ensure any follow up intents are personalized to you

.. _MD5: https://en.wikipedia.org/wiki/MD5

message contents
^^^^^^^^^^^^^^^^

**Dialogflow**

:What: A copy of your message contents only
:When: Glyph is explicitly mentioned via DM or @mention, only
:Where: Anonymously sent to Dialogflow for the sole purpose of understanding and responding to your request
:Why: To understand the intent of your message and respond

**Heroku**

:What: A copy of your message contents, your `session ids`_ (not username) and what skill Glyph interpreted it to be
:When: Glyph is explicitly mentioned via DM or @mention, only
:Where: Application logs on Heroku
:Why: To make sure Glyph is responding appropriately or see what messages caused an error

compliance
^^^^^^^^^^
:What: Your user id and opt-in/opt-out decision in a compliance category along with the time
:When: You click on "Opt in" or "Opt out"
:Where: A PostgreSQl database managed by Heroku
:Why: To respect message content data privacy decisions

configurations
^^^^^^^^^^^^^^
:What: The server id and the configuration settings
:When: A custom configuration is set by a server admin
:Where: A PostgreSQl database managed by Heroku
:Why: To access and store a server's unique configuration

server info
^^^^^^^^^^^
:What: The server name, id, member count (humans and bots)
:When: Glyph is added or removed from a server
:Where: A private Discord channel (sent via webhooks) managed by Glyph's owner
:Why: To see what servers Glyph has been added and removed from and see which servers are considered bot farms

username history
^^^^^^^^^^^^^^^^
:What: The old and new usernames of users
:When: A user changes their username
:Where: The auditing channel of any server that has Glyph configured to track username changes
:Why: So server admins can remember who people are even if they change their name


Data we don't collect
---------------------

Glyph intentionally does not provide options to "audit log" deleted and edited messages.

When you deleted a message, if Glyph interacted with it, we attempt to delete any responses it generated to it
and any copy that may be in a starboard if it was starboarded.

All audit log messages sent by Glyph to a server's logging channel are done via fire-and-forget webhooks.
The contents of these messages is never stored elsewhere.

Questions?
----------

If you have any questions or concerns about the usage and storage of your data please join the `official server`_.

.. _official server: https://gl.yttr.org/server
