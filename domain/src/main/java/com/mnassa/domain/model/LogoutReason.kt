package com.mnassa.domain.model

/**
 * Created by Peter on 5/11/2018.
 */
sealed class LogoutReason {
    class UserBlocked: LogoutReason()
    class AccountBlocked: LogoutReason()
    class NotAuthorized: LogoutReason()
    class ManualLogout: LogoutReason()
}