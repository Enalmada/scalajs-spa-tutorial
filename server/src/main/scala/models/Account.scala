package models

import models.Role.{Administrator, NormalUser}

sealed trait Role

object Role {

  case object Administrator extends Role

  case object NormalUser extends Role

  def valueOf(value: String): Role = value match {
    case "Administrator" => Administrator
    case "NormalUser" => NormalUser
    case _ => throw new IllegalArgumentException()
  }

}

case class Account(id: Int, email: String, password: String, name: String, role: Role)

/**
  * Sample model providing email/password and role to demonstrate authentication/authorization.
  * See play2-auth samples (https://github.com/t2v/play2-auth) for more options.
  */
object Account {

  val accounts = Seq(
    Account(1, "alice@example.com", "secret", "Alice", Administrator),
    Account(2, "bob@example.com", "secret", "Bob", NormalUser),
    Account(3, "chris@example.com", "secret", "Chris", NormalUser))

  def authenticate(email: String, password: String): Option[Account] = {
    accounts.find(a => a.email == email && a.password == password)
  }

}
