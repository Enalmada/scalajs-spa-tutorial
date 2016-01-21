# Server side authentication

A sample implementation of authentication and authorization is provided here as an example to get you started.  
The BasicAuthController has an `auth` function that takes an email/password in basic auth form.
It returns the role name as a string in response body and an authorization token in the response headers. 
Save the token in the client to authenticate/authorize future requests.  Delete the token to logout.

```scala
trait BasicAuthController extends Controller with AuthElement with AuthConfigImpl {

  def auth = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    Ok(loggedIn.role.toString)
  }

  def normal = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    Ok("loggedIn access")
  }

  def admin = StackAction(AuthorityKey -> Administrator) { implicit request =>
    Ok("administrator access")
  }

}
```

To test authentication manually:
```
curl -i -v -u "alice@example.com:secret" http://localhost:9000/basic/auth
```
Note Authorization header in verbose output

To test authorization of assigned roles manually:
```
curl -i -H "Authorization: Basic <token>" http://localhost:9000/basic/normal
curl -i -H "Authorization: Basic <token>" http://localhost:9000/basic/admin
```


See [play2-auth](https://github.com/t2v/play2-auth) for more auth examples and options.
For more advanced needs, consider the [silhouette](http://silhouette.mohiva.com/docs) authentication library.