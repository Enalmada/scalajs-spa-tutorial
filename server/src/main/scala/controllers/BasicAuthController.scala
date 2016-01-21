package controllers

import java.nio.charset.Charset

import jp.t2v.lab.play2.auth._
import models.Role.{Administrator, NormalUser}
import models.{Account, Role}
import org.apache.commons.codec.binary.Base64
import play.api.mvc.Results._
import play.api.mvc.{Controller, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}


/**
  * This controller provides an example for authenticating using email/password
  * and getting back a token.  To test authentication manually:
  * curl -i -v -u "alice@example.com:secret" http://localhost:9000/basic/auth
  * Note Authorization header in verbose output
  *
  * Authorization
  * curl -i -H "Authorization: Basic <token>" http://localhost:9000/basic/normal
  * curl -i -H "Authorization: Basic <token>" http://localhost:9000/basic/admin
  *
  * Sample data initialized in Global.scala
  * See play2-auth (https://github.com/t2v/play2-auth) for more auth examples.
  */
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

object BasicAuthController extends BasicAuthController


/**
  * This is a simple implementation of play2-auth config framework taken from their basic sample.
  * See play2-auth (https://github.com/t2v/play2-auth) for more information.
  */
trait AuthConfigImpl extends AuthConfig {

  type Id = Account
  type User = Account
  type Authority = Role

  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext) = Future.successful(Some(id))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext) = Future.successful((user.role, authority) match {
    case (Administrator, _) => true
    case (NormalUser, NormalUser) => true
    case _ => false
  })

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = throw new AssertionError("don't use application Login")

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = throw new AssertionError("don't use application Logout")

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful {
    Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="SECRET AREA"""")
  }

  def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit ctx: ExecutionContext) = Future.successful(Forbidden("no permission"))

  override lazy val idContainer = new BasicAuthIdContainer

  override lazy val tokenAccessor = new BasicAuthTokenAccessor

}

class BasicAuthTokenAccessor extends TokenAccessor {

  override def delete(result: Result)(implicit request: RequestHeader): Result = result

  override def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result = result

  override def extract(request: RequestHeader): Option[AuthenticityToken] = {
    val encoded = for {
      h <- request.headers.get("Authorization")
      if h.startsWith("Basic ")
    } yield h.substring(6)
    encoded.map(s => new String(Base64.decodeBase64(s), Charset.forName("UTF-8")))
  }

}

class BasicAuthIdContainer extends AsyncIdContainer[Account] {
  override def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int)(implicit request: RequestHeader, context: ExecutionContext): Future[Unit] = {
    Future.successful(())
  }

  override def get(token: AuthenticityToken)(implicit context: ExecutionContext): Future[Option[Account]] = Future {
    val Pattern = "(.*?):(.*)".r
    PartialFunction.condOpt(token) {
      case Pattern(user, pass) => Account.authenticate(user, pass)
    }.flatten
  }

  override def remove(token: AuthenticityToken)(implicit context: ExecutionContext): Future[Unit] = {
    Future.successful(())
  }

  override def startNewSession(userId: Account, timeoutInSeconds: Int)(implicit request: RequestHeader, context: ExecutionContext): Future[AuthenticityToken] = {
    throw new AssertionError("don't use")
  }
}