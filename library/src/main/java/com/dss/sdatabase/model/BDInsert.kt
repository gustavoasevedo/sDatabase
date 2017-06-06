package com.dss.sdatabase.model

import java.lang.reflect.Field

/**
 * Created by gustavo.vieira on 04/05/2015.
 */
class BDInsert {

    var field: Field? = null
    var fieldName = String()
    var fieldValue = Any()


    constructor(field: Field, fieldName: String, fieldValue: Any) {
        this.field = field
        this.fieldName = fieldName
        this.fieldValue = fieldValue
    }


    constructor()
}
