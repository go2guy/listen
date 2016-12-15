"use strict";
/**
 * Created by cgeesey on 12/6/2016.
 */

var db = require('./DbManager');
var log = require('winston');
var inspect = require('util').inspect;
const Promise = require('bluebird'); //jshint ignore: line

/**
 * Gets the template with the given id.
 * @param id
 * @return {Promise}
 */
var getProvisionerTemplate = function(id)
{
    log.debug("Attempting to get provisioner template with id "+id);
    return new Promise((resolve, reject) =>
    {
        db.query("Select * from provisioner_template where id=?", [id])
            .then((row) =>
            {
                if (row.length !== 1)
                {
                    return reject(new Error(`Row [${id}] not found`));
                }

                return resolve(row[0]);
            })
            .catch((err) =>
            {
                reject(err);
            });
    });
};

/**
 * Gets the custom fields associated with the template id provided.
 * @param id
 * @return {Promise}
 */
var getProvisionerTemplateFields = function(id)
{
    return new Promise((resolve, reject) =>
    {
        db.query("Select f.name as name, v.field_value as field_value from provisioner_template_field AS f INNER JOIN provisioner_template_field_value AS v ON f.id = v.provisioner_template_field_id where f.provisioner_template_id=?", [id])
            .then((rows) =>
            {
                resolve(rows);
            })
            .catch((err) =>
            {
                reject(err);
            });
    });
};

module.exports.getProvisionerTemplate = getProvisionerTemplate;
module.exports.getProvisionerTemplateFields = getProvisionerTemplateFields;