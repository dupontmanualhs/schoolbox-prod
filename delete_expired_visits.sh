#!/bin/sh
SQL='DELETE FROM \"VISIT\" WHERE \"EXPIRATION\" < 1000 * extract(epoch from now());'

su -c "psql -c \"${SQL}\" schoolbox"  postgres
