"use strict";

const db = require('./DbManager');
const DbUtil = require('./DbUtil');
const log = require('winston');
const inspect = require('util').inspect;
const Promise = require('bluebird'); // jshint ignore: line

/**
 * Gets the template id and other values based on the given id (mac address).
 * @param id
 * @return {Promise}
 */
var getSipPhoneDataByMac = function(mac)
{
    return new Promise((resolve, reject) =>
    {
        db.query("SELECT pn.number, sp.id, sp.provisioner_template_id, sp.organization_id, sp.username, sp.password, sp.phone_user_id, u.real_name FROM phone_number AS pn INNER JOIN sip_phone AS sp ON pn.id=sp.extension_id INNER JOIN user AS u ON pn.owner_id=u.id WHERE sp.provisioner_identifier=?", [mac])
            .then((rows) =>
            {
                if (rows.length !== 1)
                {
                    return reject(new Error(`Row [${id}] not found or more than one found.`));
                }

                resolve(rows[0]);
            })
            .catch((err) =>
            {
                reject(err);
            });
    });
};

var updateSipPhoneProvisionerLastUpdated = function(sipPhoneId, date)
{
    log.debug(`Updating Provisioner Last Updated on id [${sipPhoneId}] with date [${DbUtil.convertJSDateToMysqlDateTime(date)}]`);
    return new Promise((resolve, reject) =>
    {
        db.queryTransaction("Update sip_phone set provisioner_last_updated=? where id=?", [DbUtil.convertJSDateToMysqlDateTime(date), sipPhoneId])
            .then(() =>
            {
                resolve();
            })
            .catch(() =>
            {
                log.error(`Error updating Provisioner Last Updated: ${inspect(err)}`);
                resolve(); // Realistically, as long as the provisioning occurs, we're good. No need to reject.
            });
    });
};

module.exports.getSipPhoneDataByMac = getSipPhoneDataByMac;
module.exports.updateSipPhoneProvisionerLastUpdated = updateSipPhoneProvisionerLastUpdated;