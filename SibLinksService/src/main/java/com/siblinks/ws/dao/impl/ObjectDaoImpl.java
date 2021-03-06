/*
 * Copyright (c) 2016-2017, Tinhvan Outsourcing JSC. All rights reserved.
 *
 * No permission to use, copy, modify and distribute this software
 * and its documentation for any purpose is granted.
 * This software is provided under applicable license agreement only.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.siblinks.ws.dao.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.siblinks.ws.dao.ObjectDao;
import com.siblinks.ws.model.Download;
import com.siblinks.ws.util.CommonUtil;

/**
 *
 *
 * @author hungpd
 * @version 1.0
 */
@Repository("objectDao")
public class ObjectDaoImpl implements ObjectDao {

    private static final Logger logger = LoggerFactory.getLogger(ObjectDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment env;

    @Override
    public boolean insertUpdateObject(final String dsConfigName, final Object[] params) {
        boolean flag = true;
        try {
            logger.debug("Insert or Update " + dsConfigName);
            int numRows = jdbcTemplate.update(env.getProperty(dsConfigName), new PreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement ps) throws SQLException {
                    int i = 1;
                    for (Object value : params) {
                        ps.setObject(i, value);
                        i++;
                    }
                }
            });
            logger.debug("Insert or Update num rows" + numRows);
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public boolean insertUpdateObject(final String dsConfigName, final Map<String, String> params) {
        boolean flag = true;
        try {
            logger.debug("Insert or Update " + dsConfigName + " SQL" + env.getProperty(dsConfigName));
            int numRows = jdbcTemplate.update(env.getProperty(dsConfigName), new PreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement ps) throws SQLException {
                    int i = 1;
                    for (Entry entry : params.entrySet()) {
                        ps.setString(i, entry.getValue().toString());
                        i++;
                    }
                }
            });
            logger.debug("Insert or Update num rows" + numRows);
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    // @CacheResult(cacheName = "readObjects")
    public List readObjects(final String dsConfigName, final Object[] params) {
        logger.info("ssn " + dsConfigName + " not found in cache. TimeStamp: {}", new Date());
        List<Map<String, Object>> listUser = null;
        try {
            String query = env.getProperty(dsConfigName);
            listUser = jdbcTemplate.queryForList(query, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listUser;
    }

    @Override
    public List readObjects(final String dsConfigName, final Map<String, String> params) {
        logger.info("ssn " + dsConfigName + " not found in cache. TimeStamp: {}", new Date());
        List<Map<String, Object>> listUser = null;
        try {
            String query = env.getProperty(dsConfigName);
            listUser = jdbcTemplate.queryForList(query, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listUser;
    }

    @Override
    public boolean upload(final String dsConfigName, final Object[] params, final MultipartFile fis) {
        boolean flag = false;
        try {
            logger.info("ssn " + dsConfigName + " not found in cache. TimeStamp: {}", new Date());
            int numRows = jdbcTemplate.update(env.getProperty(dsConfigName), new PreparedStatementSetter() {

                @Override
                public void setValues(final PreparedStatement ps) throws SQLException {
                    try {
                        ps.setAsciiStream(1, fis.getInputStream(), (int) fis.getSize());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (numRows > 0) {
                flag = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return flag;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Download download(final String dsConfigName, final Object[] params) {
        List<Download> files = null;
        try {
            logger.info("ssn " + dsConfigName, new Date());
            files = jdbcTemplate.query(env.getProperty(dsConfigName), params, new RowMapper() {
                @Override
                public List<Download> mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                    Download file = new Download();
                    InputStream is = rs.getAsciiStream(1);
                    file.setInputStream(is);
                    file.setFileName(rs.getString(2));
                    file.setFilesize(rs.getString(3));
                    file.setMimeType(rs.getString(4));
                    List<Download> files = new ArrayList<Download>();
                    files.add(file);
                    return files;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (files != null) ? files.get(0) : null;
    }

    @Override
    public boolean insertObjectNotResource(final String dsConfigName, final String userId, final String itemId) {
        boolean flag = true;
        logger.info("ssn " + dsConfigName, new Date());
        PreparedStatement stmt = null;
        // CommonUtil util = CommonUtil.getInstance();
        // String query = util.getQueryNotParams(dsConfigName, userId, itemId);

        try {
            Connection conn = jdbcTemplate.getDataSource().getConnection();
            logger.debug("con===" + conn);
            stmt = conn.prepareStatement(
                env.getProperty(dsConfigName),
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
            int numRows = stmt.executeUpdate();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public List<Object> readObjectsNotResource(final String dsConfigName) {

        CommonUtil util = CommonUtil.getInstance();
        logger.info("ssn " + dsConfigName, new Date());
        String query = util.getQueryNotResource(dsConfigName);
        List<Object> listUser = new ArrayList<Object>();
        logger.debug(query);
        try {
            Connection conn = jdbcTemplate.getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                env.getProperty(dsConfigName),
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery();

            listUser = util.getObjects(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listUser;
    }

    @Override
    public String getCount(final String strSQLMapper, final Object[] params) {
        String count = "";
        try {
            String sql = env.getProperty(strSQLMapper);
            logger.info("ssn " + strSQLMapper, sql);
            SqlRowSet queryForRowSet = jdbcTemplate.queryForRowSet(sql, params);
            while (queryForRowSet.next()) {
                int cnt = queryForRowSet.getInt("COUNT");
                count = "" + cnt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
        // return listUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertObject(final String dsConfigName, final Object[] params) {
        long id = 0L;
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        try {
            logger.info("ssn " + dsConfigName, new Date() + "  SQL " + env.getProperty(dsConfigName));
            // Will hold the ID of the row created by the insert
            id = jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(env.getProperty(dsConfigName));
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject(i + 1, params[i]);
                    }
                    return ps;
                }
            }, generatedKeyHolder);
        } catch (Exception e) {
            e.printStackTrace();
            id = -1l;
        }
        id = generatedKeyHolder.getKey().longValue();
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List readObjectsWhereClause(final String dsConfigName, final String whereClause, final Object[] params) {
        logger.info("ssn " + dsConfigName + " not found in cache. TimeStamp: {}", new Date());
        List<Map<String, Object>> listUser = null;
        try {
            String query = env.getProperty(dsConfigName);
            if (whereClause != null && !"".equals(whereClause)) {
                query += whereClause;
            }
            listUser = jdbcTemplate.queryForList(query, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertUpdateBatch(final String dsConfigName, final List<Object[]> listParams) {
        logger.info("ssn " + dsConfigName + " not found in cache. TimeStamp: {}", new Date());
        try {
            String query = env.getProperty(dsConfigName);
            jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement pStmt, final int j) throws SQLException {
                    Object[] params = listParams.get(j);
                    for (int k = 0; k < params.length; k++) {
                        pStmt.setObject(k + 1, params[k]);
                    }
                }

                @Override
                public int getBatchSize() {
                    return listParams.size();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
