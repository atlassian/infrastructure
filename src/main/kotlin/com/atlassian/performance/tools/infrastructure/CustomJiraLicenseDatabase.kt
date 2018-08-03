package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.SshConnection
import java.net.URI

class CustomJiraLicenseDatabase(
    private val database: MySqlDatabase
) : Database {
    private val encodedDcLicense = """
        AAAB9g0ODAoPeNp9Ul1vozAQfPevsHRvkUwgvVzaSEh3BV+VqoUISKXex4MDm8Qt2Mg2SfPv6wDRX
        XrJIfNg787u7Mx+yhrAcW6w59ozHXvT8Q2+e8zwyPUmaK0AxEbWNSjngecgNNCCGy6FT6OMJvNkl
        lIUNdUSVLxaaFDaH7uHDwVSGJabiFXg65rtSvb6lZmSac2ZcHJZoXmj8g3TEDID/qEd8Vx7UN8o2
        9fQokP6RB/iOU2OEfpWc7XvYJ5rMSNy5R0b0kfGywsdU1BbULPQv6UTSj6HP0bkS3w/IXdXN9foh
        Svm1EoWTW6cw4VouTI7psCxZfkWfKMaQPc2Mu+zLAUWgDAfUtJmqXPF61an9uWMjmfGaxlclnkwG
        ERxRr7HCZkncbgIslkckUVKbcAPFFg9CrzcY7MB3FfBVOSyAIXtXC9gXf65Mab+NR0O19I50WZYd
        ggCHeK3g0OJhTS44NoovmwM2MpcYyNx3mgjK2u2g6zawoBgIj+xxfWuezctuSCh3zIaktvnA9Pz1
        vxH/bPr1Q9ovVyIVyF3AqU08u1PbEJX7egOqM6EWK2Z4Jp1ur6xqi4BB7KqmdijVj8b+LiMIfyxM
        gNtcK8TXkmratmsucAFbKGU1lt9YvTfK9ES+ufh/LyXF4RuWdl0/Fes1IDeAciwSo0wLAIUfK/4z
        z0xVAzjtY/CUb6x3zbNBJcCFGqkFL1CHBMghNzG3CN4nfwH4VPpX02nn
        """.lines().joinToString(separator = "") { it.trim() }

    private val encodedServiceDeskLicense = """
        AAAB7w0ODAoPeNqFUl1vmzAUfedXWNpbJJPgtmsbCWkruFWqFhAhlbqPBwM3iRuwkW3S5t/XAaIuU
        7NZ8oN9P86559wv2bpFt5AjQpBHppPzqUfQ3WOGyMS7cl64Ym6jZNkWxt0/sAa15QWUoDcuKwzfg
        m9UC07U1jmoeLmwCdr3JvvjBFIYmxSxGnzdsNeKbb4xUzGtORNuIWvHXvfj51TzpFXFmmkImQF/T
        wxPCCbEebDJQkO2a6DDCOkTfYgTmh4i9K3hajeUkQvsEXzmHWjRR8arE7zmlgmoWejf0EuKz8MfB
        H+N7y/x3dn1lXNvhUgGUWxvFoAwCo4Yn57rNOd/i326bt7mulC8MVyKHr3rdJCg5F2ARhlNk3Q2p
        /8B+tTJ0WgUxRm+jVOcpHG4CLJZHOHFnNqAHyiwCpco3yGzBjQAIyoKWYJCFukFCoN+ro1pfk3H4
        5V0j9QeV30Fhr7it4tCiYQ0qOTaKJ63BmxnrpGRqGi1kbWl5jrWP2FAMFEcG+1dD/thyQUp/Z7RE
        N8875l+bvZA2Lq9EBshX4Uzp5FvL76wo3dqHWwG1SscqxUTXLNe2jdWNxWgQNYNEzun08MG/l7XE
        D58ykAbNMyNltKqVLUrLlAJW6hkY+frgf9cKrplVdtDLlmlwXkHTmNLCDAtAhUAkbnUW/514ZdhN
        XOVOORkun2qJawCFCIevWM4tEF2UDfS90zkANJ9zUmTX02nf
        """.lines().joinToString(separator = "") { it.trim() }

    override fun setup(
        ssh: SshConnection
    ): String {
        return database.setup(ssh)
    }

    override fun start(
        jira: URI,
        ssh: SshConnection
    ) {
        database.start(jira, ssh)
        ssh.execute("""mysql -h 127.0.0.1  -u root -e "DELETE FROM jiradb.productlicense; REPLACE INTO jiradb.productlicense (LICENSE) VALUES (\"$encodedDcLicense\");" """)
        ssh.execute("""mysql -h 127.0.0.1  -u root -e "insert into jiradb.productlicense select max(id)+1, \"$encodedServiceDeskLicense\" from jiradb.productlicense;;" """)
    }
}
