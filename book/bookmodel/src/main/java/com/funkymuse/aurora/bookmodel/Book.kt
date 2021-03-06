package com.funkymuse.aurora.bookmodel

import com.funkymuse.aurora.generalbook.GeneralBook


/**
 * Created by FunkyMuse on 25/02/21 to long live and prosper !
 */
data class Book(
    override val image: String?,
    override val title: String?,
    override val author: String?,
    override val id: String,
    override val extension: String?,
    override val pages: String?,
    override val size: String?,
    override val year: String?
) : GeneralBook