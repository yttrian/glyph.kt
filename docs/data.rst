Data Collection
===============

We understand that when you use a bot, there's a certain level of privacy you expect.
That is why we provide this information below, to clear up any concerns.


Data We Collect
---------------

session ids
^^^^^^^^^^^
:What: An MD5 hash of your user ID and the channel ID
:When: Glyph is explicitly mentioned via DM or @mention
:Where: DialogFlow requests and message logs
:Why: Used with DialogFlow to ensure any follow up intents are personalized to you

messages
^^^^^^^^
:What: A copy of your message contents, your _`session ID` and what skill Glyph interpreted it to be
:When: Glyph is explicitly mentioned via DM or @mention, only
:Where: Temporary log on Heroku, logs generally disappear within 24 hours
:Why: To make sure Glyph is responding appropriately or see what messages caused an error

configurations
^^^^^^^^^^^^^^
:What: The server ID and the configuration settings
:When: A custom configuration is set by a server admin (deleted when Glyph is removed from the server)
:Where: A PostgreSQl database managed by Heroku
:Why: To access and store a server's unique configuration

server info
^^^^^^^^^^^
:What: The server name, ID, member count (humans and bots)
:When: Glyph is added or removed from a server
:Where: A private Discord channel (sent via webhooks)
:Why: To see what servers Glyph has been added and removed from and see which servers are considered bot farms

username history
^^^^^^^^^^^^^^^^
:What: The old and new usernames of users
:When: A user changes their username
:Where: The auditing channel of any server that has Glyph configured to track username changes
:Why: So server admins can remember who people are even if they change their name


Data We Don't Collect
---------------------

Obviously anything we don't collect is not listed above but here's a list of common things we don't collect.

- Deleted messages
- Message edits
- The username, ID, or discriminator of who sent feedback
- Audit logs (once they are sent, they are forgotten)
- Ban reasons
- Kick reasons