/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2020 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.banking

import de.micromata.genome.db.jpa.history.api.WithHistory
import org.hibernate.search.annotations.*
import org.projectforge.framework.persistence.entities.DefaultBaseDO
import java.math.BigDecimal
import java.sql.Date
import javax.persistence.*

/**
 * Die Kontostände für ein Konto zu festen Zeitpunkten soll die Integrität der Kontobewegungen sicherstellen. So kann
 * bei abweichenden Kontoständen geprüft werden, in welcher Periode abweichende Kontodaten importiert wurden.
 *
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_BANK_ACCOUNT_BALANCE", indexes = [javax.persistence.Index(name = "idx_fk_t_plugin_bank_account_balance_tenant_id", columnList = "tenant_id")])
@WithHistory
open class BankAccountBalanceDO : DefaultBaseDO() {

    @IndexedEmbedded(depth = 1)
    @get:ManyToOne(fetch = FetchType.LAZY)
    @get:JoinColumn(name = "account_fk", nullable = false)
    open var account: BankAccountDO? = null

    @Field(analyze = Analyze.NO)
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    @get:Column(name = "date_col", nullable = false)
    open var date: Date? = null

    @get:Column(nullable = false, scale = 5, precision = 18)
    open var amount: BigDecimal? = null

    @Field
    @get:Column(length = 4000)
    open var description: String? = null
}
