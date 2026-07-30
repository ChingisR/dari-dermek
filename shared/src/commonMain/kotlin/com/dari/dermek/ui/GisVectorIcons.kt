package com.dari.dermek.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Original domain vector icons drawn on a 24x24 grid.
 * The bundled Material icon set has no veterinary/regulatory glyphs, so the
 * app used to fall back on hearts and stars. These replace them everywhere.
 */
private fun gisVector(name: String, body: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(body).build()

private fun ImageVector.Builder.filled(
    fillType: PathFillType = PathFillType.EvenOdd,
    body: PathBuilder.() -> Unit
): ImageVector.Builder = path(
    fill = SolidColor(Color.Black),
    pathFillType = fillType,
    pathBuilder = body
)

private fun PathBuilder.rect(l: Float, t: Float, r: Float, b: Float) {
    moveTo(l, t)
    lineTo(r, t)
    lineTo(r, b)
    lineTo(l, b)
    close()
}

private fun PathBuilder.roundRect(l: Float, t: Float, r: Float, b: Float, rad: Float) {
    if (rad <= 0f) {
        rect(l, t, r, b)
        return
    }
    moveTo(l + rad, t)
    lineTo(r - rad, t)
    arcToRelative(rad, rad, 0f, false, true, rad, rad)
    lineTo(r, b - rad)
    arcToRelative(rad, rad, 0f, false, true, -rad, rad)
    lineTo(l + rad, b)
    arcToRelative(rad, rad, 0f, false, true, -rad, -rad)
    lineTo(l, t + rad)
    arcToRelative(rad, rad, 0f, false, true, rad, -rad)
    close()
}

private fun PathBuilder.circle(cx: Float, cy: Float, rad: Float) {
    moveTo(cx - rad, cy)
    arcToRelative(rad, rad, 0f, true, true, rad * 2, 0f)
    arcToRelative(rad, rad, 0f, true, true, -rad * 2, 0f)
    close()
}

private fun PathBuilder.shieldOutline() {
    moveTo(12f, 2.2f)
    lineTo(20f, 5.4f)
    lineTo(20f, 11.4f)
    curveTo(20f, 16.6f, 16.7f, 20.5f, 12f, 21.8f)
    curveTo(7.3f, 20.5f, 4f, 16.6f, 4f, 11.4f)
    lineTo(4f, 5.4f)
    close()
}

private fun PathBuilder.qrFinder(x: Float, y: Float) {
    roundRect(x, y, x + 7.4f, y + 7.4f, 1.8f)
    roundRect(x + 1.6f, y + 1.6f, x + 5.8f, y + 5.8f, 1f)
    roundRect(x + 3f, y + 3f, x + 4.4f, y + 4.4f, 0.4f)
}

object GisVectorIcons {

    /** Capsule — drug registry. */
    val Capsule: ImageVector by lazy {
        gisVector("gis_capsule") {
            group(rotate = -45f, pivotX = 12f, pivotY = 12f) {
                filled {
                    roundRect(2.6f, 8.5f, 21.4f, 15.5f, 3.5f)
                    roundRect(11.4f, 9f, 12.6f, 15f, 0.4f)
                }
            }
        }
    }

    /** Clipboard with lines — applications / dossiers. */
    val Clipboard: ImageVector by lazy {
        gisVector("gis_clipboard") {
            filled {
                roundRect(3.8f, 3.2f, 20.2f, 21.4f, 2.6f)
                roundRect(8.2f, 5.2f, 15.8f, 8.2f, 1.2f)
                roundRect(7.4f, 11f, 16.6f, 12.6f, 0.8f)
                roundRect(7.4f, 14.8f, 13.6f, 16.4f, 0.8f)
            }
        }
    }

    /** QR code — scanner / traceability. */
    val QrCode: ImageVector by lazy {
        gisVector("gis_qr") {
            filled {
                qrFinder(3f, 3f)
                qrFinder(13.6f, 3f)
                qrFinder(3f, 13.6f)
                roundRect(13.6f, 13.6f, 16.4f, 16.4f, 0.6f)
                roundRect(18.2f, 13.6f, 21f, 16.4f, 0.6f)
                roundRect(13.6f, 18.2f, 16.4f, 21f, 0.6f)
                roundRect(18.2f, 18.2f, 21f, 21f, 0.6f)
            }
        }
    }

    /** Shield with a check — verified / state authority. */
    val ShieldCheck: ImageVector by lazy {
        gisVector("gis_shield_check") {
            filled {
                shieldOutline()
                moveTo(10.9f, 16.1f)
                lineTo(7.4f, 12.6f)
                lineTo(8.8f, 11.2f)
                lineTo(10.9f, 13.3f)
                lineTo(15.4f, 8.8f)
                lineTo(16.8f, 10.2f)
                close()
            }
        }
    }

    /** Shield with a heart — pharmacovigilance. */
    val ShieldHeart: ImageVector by lazy {
        gisVector("gis_shield_heart") {
            filled {
                shieldOutline()
                moveTo(12f, 18.2f)
                curveTo(8.4f, 15.4f, 6.3f, 13.5f, 6.3f, 11.2f)
                curveTo(6.3f, 9.4f, 7.7f, 8f, 9.5f, 8f)
                curveTo(10.5f, 8f, 11.4f, 8.6f, 12f, 9.6f)
                curveTo(12.6f, 8.6f, 13.5f, 8f, 14.5f, 8f)
                curveTo(16.3f, 8f, 17.7f, 9.4f, 17.7f, 11.2f)
                curveTo(17.7f, 13.5f, 15.6f, 15.4f, 12f, 18.2f)
                close()
            }
        }
    }

    /** Shield with a medical cross — regulator / logo mark. */
    val ShieldCross: ImageVector by lazy {
        gisVector("gis_shield_cross") {
            filled {
                shieldOutline()
                moveTo(10.5f, 7.6f)
                lineTo(13.5f, 7.6f)
                lineTo(13.5f, 10.6f)
                lineTo(16.5f, 10.6f)
                lineTo(16.5f, 13.6f)
                lineTo(13.5f, 13.6f)
                lineTo(13.5f, 16.6f)
                lineTo(10.5f, 16.6f)
                lineTo(10.5f, 13.6f)
                lineTo(7.5f, 13.6f)
                lineTo(7.5f, 10.6f)
                lineTo(10.5f, 10.6f)
                close()
            }
        }
    }

    /** Laboratory flask — expertise / testing. */
    val Flask: ImageVector by lazy {
        gisVector("gis_flask") {
            filled(PathFillType.NonZero) {
                moveTo(9.4f, 2.4f)
                lineTo(14.6f, 2.4f)
                lineTo(14.6f, 4.2f)
                lineTo(13.7f, 4.2f)
                lineTo(13.7f, 9.8f)
                lineTo(18.9f, 17.9f)
                curveTo(20.1f, 19.8f, 18.8f, 21.6f, 16.6f, 21.6f)
                lineTo(7.4f, 21.6f)
                curveTo(5.2f, 21.6f, 3.9f, 19.8f, 5.1f, 17.9f)
                lineTo(10.3f, 9.8f)
                lineTo(10.3f, 4.2f)
                lineTo(9.4f, 4.2f)
                close()
            }
        }
    }

    /** Syringe — immunobiological products. */
    val Syringe: ImageVector by lazy {
        gisVector("gis_syringe") {
            group(rotate = -45f, pivotX = 12f, pivotY = 12f) {
                filled(PathFillType.NonZero) {
                    roundRect(6.4f, 9.4f, 15.4f, 14.6f, 1.2f)
                    rect(15.4f, 11.3f, 17.8f, 12.7f)
                    rect(17.8f, 11.7f, 22.2f, 12.3f)
                    roundRect(5.1f, 8.4f, 6.6f, 15.6f, 0.5f)
                    roundRect(2.2f, 10.4f, 5.1f, 13.6f, 0.8f)
                }
            }
        }
    }

    /** Spray bottle — disinfectants. */
    val Spray: ImageVector by lazy {
        gisVector("gis_spray") {
            filled(PathFillType.NonZero) {
                roundRect(7.2f, 9f, 15.6f, 21.6f, 2.2f)
                rect(9.6f, 5.4f, 13.2f, 9.4f)
                roundRect(13.2f, 4f, 17.4f, 6.4f, 0.8f)
                circle(20.2f, 3.4f, 1f)
                circle(21.6f, 6.8f, 0.85f)
                circle(19.6f, 9.2f, 0.7f)
            }
        }
    }

    /** Feed bowl — feed additives. */
    val FeedBowl: ImageVector by lazy {
        gisVector("gis_feed_bowl") {
            filled(PathFillType.NonZero) {
                moveTo(2.6f, 11.6f)
                lineTo(21.4f, 11.6f)
                curveTo(21.4f, 17.3f, 17.2f, 21.2f, 12f, 21.2f)
                curveTo(6.8f, 21.2f, 2.6f, 17.3f, 2.6f, 11.6f)
                close()
                circle(8.2f, 7.6f, 1.5f)
                circle(12f, 5.6f, 1.7f)
                circle(15.8f, 7.6f, 1.5f)
            }
        }
    }

    /** Delivery truck — import / border control. */
    val Truck: ImageVector by lazy {
        gisVector("gis_truck") {
            filled(PathFillType.NonZero) {
                roundRect(1.6f, 6.4f, 13.4f, 16.6f, 1.6f)
                moveTo(14.6f, 9.4f)
                lineTo(18.4f, 9.4f)
                lineTo(22.4f, 13.2f)
                lineTo(22.4f, 16.6f)
                lineTo(14.6f, 16.6f)
                close()
                circle(6f, 18.2f, 2.2f)
                circle(17.6f, 18.2f, 2.2f)
            }
        }
    }

    /** Warehouse — storage and distribution. */
    val Warehouse: ImageVector by lazy {
        gisVector("gis_warehouse") {
            filled {
                moveTo(12f, 2.6f)
                lineTo(22f, 7.6f)
                lineTo(22f, 10.2f)
                lineTo(20f, 10.2f)
                lineTo(20f, 21.4f)
                lineTo(4f, 21.4f)
                lineTo(4f, 10.2f)
                lineTo(2f, 10.2f)
                lineTo(2f, 7.6f)
                close()
                roundRect(8.6f, 13.4f, 15.4f, 21f, 0.6f)
            }
        }
    }

    /** Package — inventory / empty state. */
    val Package: ImageVector by lazy {
        gisVector("gis_package") {
            filled {
                moveTo(12f, 2.2f)
                lineTo(21.2f, 7.1f)
                lineTo(21.2f, 17.5f)
                lineTo(12f, 22.4f)
                lineTo(2.8f, 17.5f)
                lineTo(2.8f, 7.1f)
                close()
                moveTo(12f, 12.4f)
                lineTo(19.2f, 8.5f)
                lineTo(19.2f, 16.4f)
                lineTo(12f, 20.2f)
                close()
            }
        }
    }

    /** Office building — organisations. */
    val Building: ImageVector by lazy {
        gisVector("gis_building") {
            filled {
                roundRect(3.6f, 2.8f, 20.4f, 21.4f, 2f)
                roundRect(6.6f, 6.2f, 10.4f, 9.2f, 0.7f)
                roundRect(13.6f, 6.2f, 17.4f, 9.2f, 0.7f)
                roundRect(6.6f, 11.4f, 10.4f, 14.4f, 0.7f)
                roundRect(13.6f, 11.4f, 17.4f, 14.4f, 0.7f)
                roundRect(9.8f, 17f, 14.2f, 21f, 0.8f)
            }
        }
    }

    /** Bar chart — analytics and reporting. */
    val BarChart: ImageVector by lazy {
        gisVector("gis_bar_chart") {
            filled(PathFillType.NonZero) {
                roundRect(3.2f, 12.6f, 7.4f, 20.8f, 1.1f)
                roundRect(9.9f, 8.2f, 14.1f, 20.8f, 1.1f)
                roundRect(16.6f, 4.2f, 20.8f, 20.8f, 1.1f)
            }
        }
    }

    /** Receipt — prescriptions and acts. */
    val Receipt: ImageVector by lazy {
        gisVector("gis_receipt") {
            filled {
                moveTo(4.4f, 2.6f)
                lineTo(19.6f, 2.6f)
                lineTo(19.6f, 21.6f)
                lineTo(16.6f, 19.7f)
                lineTo(13.6f, 21.6f)
                lineTo(10.4f, 19.7f)
                lineTo(7.4f, 21.6f)
                lineTo(4.4f, 19.7f)
                close()
                roundRect(7.2f, 6.8f, 16.8f, 8.4f, 0.6f)
                roundRect(7.2f, 10.8f, 16.8f, 12.4f, 0.6f)
                roundRect(7.2f, 14.8f, 13.2f, 16.4f, 0.6f)
            }
        }
    }

    /** Gavel — expert committee decisions. */
    val Gavel: ImageVector by lazy {
        gisVector("gis_gavel") {
            group(rotate = 35f, pivotX = 12f, pivotY = 12f) {
                filled(PathFillType.NonZero) {
                    roundRect(8f, 4.4f, 16f, 8.6f, 1.4f)
                    roundRect(10.6f, 8.6f, 13.4f, 19.5f, 1.2f)
                }
            }
        }
    }

    /** Leaf — farms and agriculture. */
    val Leaf: ImageVector by lazy {
        gisVector("gis_leaf") {
            filled(PathFillType.NonZero) {
                moveTo(4.6f, 19.6f)
                curveTo(3.2f, 12.4f, 8.4f, 4.2f, 20.2f, 3.6f)
                curveTo(21f, 14.8f, 14.2f, 21.6f, 6.4f, 19.8f)
                curveTo(8.4f, 14.6f, 12.2f, 10.6f, 16.4f, 8.2f)
                curveTo(11.4f, 9.4f, 6.8f, 13.8f, 4.6f, 19.6f)
                close()
            }
        }
    }

    /** Disposal bin — write-off and destruction acts. */
    val Recycle: ImageVector by lazy {
        gisVector("gis_disposal_bin") {
            filled {
                roundRect(9.2f, 2.2f, 14.8f, 4.4f, 0.8f)
                roundRect(3.2f, 4.6f, 20.8f, 7.6f, 1.2f)
                moveTo(5.4f, 8.8f)
                lineTo(18.6f, 8.8f)
                lineTo(17.3f, 21.8f)
                lineTo(6.7f, 21.8f)
                close()
                roundRect(8.9f, 11.2f, 10.6f, 19.4f, 0.8f)
                roundRect(13.4f, 11.2f, 15.1f, 19.4f, 0.8f)
            }
        }
    }

    /** Clock — SLA timers and schedules. */
    val Clock: ImageVector by lazy {
        gisVector("gis_clock") {
            filled {
                circle(12f, 12f, 9.6f)
                circle(12f, 12f, 7.9f)
                moveTo(11.1f, 5.9f)
                lineTo(12.9f, 5.9f)
                lineTo(12.9f, 11.7f)
                lineTo(17.2f, 14.2f)
                lineTo(16.3f, 15.7f)
                lineTo(11.1f, 12.7f)
                close()
            }
        }
    }
}
