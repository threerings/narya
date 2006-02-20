/**
 * $Id$
 *
 * Schema for the Admin services configuration table.
 */

drop table if exists CONFIG;

/**
 * Contains all registered configuration data.
 */
CREATE TABLE CONFIG
(
    /** The configuration object with which this datum is associated. */
    OBJECT VARCHAR(255) NOT NULL,

    /** The configuration object field name. */
    FIELD VARCHAR(255) NOT NULL,

    /** The value of the object field. */
    VALUE TEXT NOT NULL,

    /** Defines our table keys. */
    PRIMARY KEY (OBJECT, FIELD)
);
