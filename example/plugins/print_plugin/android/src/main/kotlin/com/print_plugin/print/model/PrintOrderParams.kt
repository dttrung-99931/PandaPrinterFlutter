package com.print_plugin.print.model

import com.google.gson.annotations.SerializedName
import java.util.Date


data class PrintOrderParams(
    @SerializedName("form") var form: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("new_issued") var newIssued: Boolean = false,
    @SerializedName("data") var data: Data? = Data(),
)

data class Data(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("clinicId") var clinicId: Int? = null,
    @SerializedName("clinicName") var clinicName: String? = null,
    @SerializedName("orderCode") var orderCode: String? = null,
    @SerializedName("clinicCode") var clinicCode: String? = null,
    @SerializedName("setDate") var setDate: Date? = null,
    @SerializedName("setTime") var setTime: Date? = null,
    @SerializedName("patientName") var patientName: String? = null,
    @SerializedName("products") var products: ArrayList<Product> = arrayListOf(),
    @SerializedName("materials") var materials: ArrayList<Material> = arrayListOf(),
    @SerializedName("processes") var processes: ArrayList<Process> = arrayListOf(),
)

data class Product(
    @SerializedName("id") var id: Long? = null,
    @SerializedName("productClassName") var productClassName: String? = null,
    @SerializedName("productName") var productName: String? = null,
    @SerializedName("productCode") var productCode: String? = null,
    @SerializedName("markLabel") var markLabel: String? = null,
    @SerializedName("quantity") var quantity: Int? = null,
    @SerializedName("teeth") var teeth: Teeth? = Teeth(),
    @SerializedName("breakdowns") var breakdowns: ArrayList<Breakdown> = arrayListOf(),
    @SerializedName("showCodeFlg") var showCodeFlg: Boolean? = null,
)

data class Material(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("code") var code: String? = null,
    @SerializedName("weight") var weight: Double? = null,
    @SerializedName("quantity") var quantity: Int? = null,
)

data class Process(
    @SerializedName("processName") var processName: String? = null,
    @SerializedName("staffName") var staffName: String? = null,
)

data class Teeth(
    @SerializedName("topLeft") var topLeft: String? = null,
    @SerializedName("topRight") var topRight: String? = null,
    @SerializedName("bottomLeft") var bottomLeft: String? = null,
    @SerializedName("bottomRight") var bottomRight: String? = null,
)

data class Breakdown(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("teeth") var teeth: Teeth? = Teeth(),
    @SerializedName("name") var name: String? = null,
    @SerializedName("code") var code: String? = null,
    @SerializedName("quantity") var quantity: Int? = null,
    @SerializedName("showCodeFlg") var showCodeFlg: Boolean? = null,
)

data class QrCodeLoginParams(
    @SerializedName("title") var title: String? = null,
    @SerializedName("content") var content: String? = null,
    @SerializedName("data") var data: String? = null,
)