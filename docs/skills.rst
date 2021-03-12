Skills
======
Here is the current list of everything Glyph can do and how to ask him to do it.

If you have a feature you'd like to have added, suggest it in the `official server`_ or use Feedback_.


Wiki
----
Glyph can search a specified Wikia_ wiki or Wikipedia_ when asked what something is. This is set in the configuration. By default this is set to Wikipedia and can only be changed in servers that have a properly set up :doc:`/configuration`. Private messages will always use Wikipedia.

.. _Wikia: https://www.wikia.com/
.. _Mass Effect Wiki: https://masseffect.wikia.com/wiki/Mass_Effect_Wiki
.. _Wikipedia: https://en.wikipedia.org

Example phrases:

- Who is **Garrus Vakarian** according to **masseffect** wiki?
- What are **trilobites**?
- Who is **Bill Nye**?

Moderation
----------
How about you get some help moderating your server?

Kick
^^^^
Kick members with ease on mobile and on desktop!

Example phrases:

- kick **@SomeUser#0001** for stealing a sweet roll
- kick **@SomeUser#0001** **@SomeUser#0001**
- kick **@SomeUser#0001**

.. note::
  Only people with the permission :code:`Kick Members` can ask Glyph to kick members.
  A few additional checks are also made to ensure people can not kick members of the same role or higher, or themselves.

Ban
^^^
Ban members with ease on mobile and on desktop!

Example phrases:

- ban **@SomeUser#0001** for being annoying
- ban **@SomeUser#0001** **@SomeUser#0002**
- ban **@SomeUser#0001**

.. note::
  Only people with the permission :code:`Ban Members` can ask Glyph to ban members.
  A few additional checks are also made to ensure people can not ban members of the same role or higher, or themselves.


Purge
^^^^^
Mass remove messages within a certain time frame ago. Simply specify how far back you want to delete in days, hours, and or minute.
You can also provide an optional reason as shown below.

Example phrases:

- purge 14 days
- purge 5 min
- purge 3h for spam

.. note::
  Only people with the permission :code:`Manage Messages` can ask Glyph to purge messages.

User Info
^^^^^^^^^
Ever wanted to know more info about a user, or want to brag about how old your account is?

Example phrases:

- user info **@SomeUser#0001**
- user info
- user info for **@SomeUser#0001**

Server Info
^^^^^^^^^^^
Glyph can create a nice little embed with all the info about a server, or you can ask for a specific piece of information.
Some specific pieces of information you can ask for are the server's name, owner, id, region, total members, and more.

Example phrases:

- who is the owner of this server?
- server info
- how old is this server?

Role
----
Glyph can assign roles as set in the :doc:`/configuration`, typically for cosmetic/vanity/roleplay purposes, when asked by a user.

Set
^^^
To give someone a role from the list of available roles.

Example phrases:

- make me **Geth**.
- set me as **Turian**.
- set **@SomeUser#0001** as **notify**.

.. note::
  Only people with the permission :code:`Manage Roles` can ask Glyph to assign other peoples' roles.
  Additional, they can violate a limit that is greater than 1 when giving people extra roles.

Unset
^^^^^
To remove a role from the list of available roles from someone.

Example phrases:

- remove me from **Geth**.
- remove **Turian** from me
- remove **@SomeUser#0001** from **notify**.

.. note::
  Only people with the permission :code:`Manage Roles` can ask Glyph to remove other peoples' roles.

List
^^^^
To list the available roles.

Example phrases:

- roles list
- list roles

Images
------
Glyph can pull images from Reddit_ when asked. As of right now, these can either be memes, cats, snakes, birds, dogs and lizards.

.. _Reddit: https://reddit.com/

Example phrases:

- image from **coaxedintoasnafu**
- image from **youdontsurf**
- **meme**

QuickView
---------
Glyph can post helpful embeds that include additional information for links from `Fur Affinity`_ and Picarto_. This feature can be controlled in the :doc:`/configuration`.

.. _Fur Affinity: https://furaffinity.net/
.. _Picarto: https://picarto.tv/

Fur Affinity
^^^^^^^^^^^^
Gives some quick info about Fur Affinity submissions such as the category, the species, the gender, the number of favorites, comments, and views, and the keywords.

Picarto
^^^^^^^
Gives some quick info about Picarto streams such as the online status, the category, the number of viewers and followers, and the tags.

Starboard
---------
Glyph can send messages that are reacted upon with a chosen emoji (:star: by default) to another channel, kind of like pinning, but for everyone and with no limit!
This feature must be configured in the :doc:`/configuration` first before it can be used.

Status
------
Glyph can show his current ping and total number of servers he's in when asked.

Example phrases:

- ping
- pong
- status

Time
----
Glyph can show you the time when provided with any time zone in the `tz database`_. The detection is not always accurate and will fallback to UTC.

.. _tz database: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones

Example phrases:

- time
- time Hong Kong
- EST time

Ephemeral Say
-------------
Ever wanted to say something, but only for a brief amount of time (kinda like a popular messaging app), and are too lazy to delete it yourself?
You can ask Glyph to say something on your behalf, and then Glyph will delete it after a short amount of time (30 seconds max).

Example phrases:

- say "something embarrassing" for 10 seconds
- say "nobody will notice this" for 5 seconds
- say "that's rather odd" for 20 seconds

Help
----
Glyph can provide some quick help information when asked.

Example phrases:

- help
- help me
- What can you do?


.. _Feedback:

Feedback
---------
Having an issue with a skill, or have a suggestion for how to improve the Glyph? Send some feedback!

Example phrases:

- send feedback "This feature needs some work"
- send feedback "I love Glyph, but..."
- send feedback "Something that is useful sounding."

.. note::
  All feedback is sent anonymously via a webhook to a Discord channel meant for collecting feedback.
  We will not be able to reply to you. If you need a reply, join the `official server`_.

Canned Responses
----------------
Sometimes if a message does not fall under a specific skill, Glyph will try to reply appropriately.

Others
------
There are a few other tiny fun/easter egg skills that are so small they don't deserve a whole section detailing them.
Some hints are to what they are are NumberWang, Dad jokes, jokes, the Doomsday Clock, `snowstamp`_, magic 8-ball and a few others.

.. _snowstamp: https://pixelatomy.com/snow-stamp/
.. _official server: https://gl.yttr.org/server