package com.mnassa.domain.model

/**
 * Created by Peter on 5/11/2018.
 */
sealed class UserStatusModel {
    class Enabled : UserStatusModel()
    class Disabled : UserStatusModel()
}