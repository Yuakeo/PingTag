# PingTag

PingTag is an addon for the modification Labymod, this renders a tag above the head of a player with his ping.

# Server Support

You can disable PingTag on the server side by sending a Json to the [LMC](http://docs.labymod.net/pages/server/protocol/) Channel.

The message key is \"pingtag\"  
The Json must look like this:
```json
{
 "allowed": false
}
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.