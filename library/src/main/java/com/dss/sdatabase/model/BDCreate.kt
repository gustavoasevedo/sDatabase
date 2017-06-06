package com.dss.sdatabase.model

/**
 * Created by gustavo.vieira on 04/05/2015.
 */
class BDCreate {

    var fieldName: String? = ""
    var fieldType: String? = ""

    constructor(fieldName: String, fieldType: String) {
        this.fieldName = fieldName
        this.fieldType = fieldType
    }

    constructor()
}
