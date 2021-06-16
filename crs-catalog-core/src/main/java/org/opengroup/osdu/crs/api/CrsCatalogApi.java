// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Copyright 2016 Schlumberger. All Rights Reserved.
 *
 */

package org.opengroup.osdu.crs.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.logging.AuditLogger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.opengroup.osdu.crs.model.EssenceImpl;
import org.opengroup.osdu.crs.util.AppException;
import org.opengroup.osdu.crs.model.AreaOfUseEssenceImpl;
import org.opengroup.osdu.crs.model.EarlyBoundCRSEssenceImpl;
import org.opengroup.osdu.crs.model.CompoundCRSEssenceImpl;
import org.opengroup.osdu.crs.model.LateBoundCRSEssenceImpl;
import org.opengroup.osdu.crs.model.CompoundCTEssenceImpl;
import org.opengroup.osdu.crs.model.SphericalBoundingBoxImpl;
import org.opengroup.osdu.crs.model.interfaces.*;
import org.opengroup.osdu.crs.model.request.*;
import org.opengroup.osdu.crs.model.search.*;

/**
 *  CrsApi is an endpoint class we are exposing so that clients can request or query for
 *  coordinate reference system related catalog data information through its various
 *  exposed API methods
 */

@RestController
@RequestMapping(path= "/v2")
public class CrsCatalogApi {

	private Catalog catalog = null;

	@Inject
	private AuditLogger auditLogger;
	@Inject
	private JaxRsDpsLog log;

	public CrsCatalogApi(Catalog catalog) {
		this.catalog = catalog;
	}

    /**
     * get the entire catalog content
      * @return Catalog
     */
	@GetMapping("/catalog")
	public Catalog getCatalog() {
   		return catalog.getCatalogResponse();
	}

    /**
     * get the catalog attributes
     * @return catalog attributes
     */
	@GetMapping("/catalog/attributes")
	public CatalogAttributes getCatalogAttributes() {
   		return catalog.getAttributes();
	}

    /**
     * get a list of AreasOfUses in the catalog
     * @param offset  Offset of the first item in the AreasOfUses. It is optional and is 0 by default.
     * @param limit   Maximum number of the AreasOfUse items returned. It is optional and is 100 by default.
     * @return AreasOfUseResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@GetMapping("/area")
	public AreaOfUseResults getAreasOfUse(
		@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
	    @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
		@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		AreaOfUseResults	results = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			List<AreaOfUse> list = catalog.getAreasOfUse();
			if(list != null) {
				if ((offset < 0) || ((list.size() > 0) && (offset >= list.size())))
					throw new IndexOutOfBoundsException("The start index is out of the range");

				if ((limit < 0) || (limit > list.size() - offset))
					limit = list.size() - offset;

				if (limit >= 0) {
					ArrayList<AreaOfUse> list2 = new ArrayList<>();
					for (int i = offset; i < offset + limit; i++) {
						list2.add(list.get(i).convert(mode));
					}
					results = new AreaOfUseResults(list2, offset, list.size());
				}
			}
		}
		catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(results)) {
			this.auditLogger.readAreaSuccess(Collections.singletonList(results.toString()));
		} else {
			this.auditLogger.readAreaSuccess(Collections.singletonList("AreaOfUseResults is null"));
		}
		return results;
	}

    /**
     * get the AreaOfUse by essence
     * @param request AreaOfUseRequest
     * @return AreaOfUse
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/area")
	public AreaOfUse getAreaOfUse(@RequestBody AreaOfUseRequest request,
								  @RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		AreaOfUseEssence essence = null;
		AreaOfUse aou = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			essence = request.getAreaOfUseEssence();
			if (essence == null) {
				EssenceImpl ei = EssenceImpl.createInstance(request.getPersistableReference());
				essence = (AreaOfUseEssenceImpl)ei;
			}
			aou = catalog.getAreaOfUse(essence);
			if(null != aou){
				aou = aou.convert(mode);
			}
		}
		catch (Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(aou)) {
			this.auditLogger.readAreaByEssenceSuccess(Collections.singletonList(aou.toString()));
		} else {
			this.auditLogger.readAreaByEssenceSuccess(Collections.singletonList("AreaOfUse is null"));
		}
		return aou;
	}

    /**
     * get a list of CRSes in the catalog
     * @param offset  Offset of the first item in the crs. It is optional and is 0 by default.
     * @param limit   Maximum number of the crs items returned. It is optional and is 100 by default.
     * @return CRSResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@GetMapping("/crs")
	public CRSResults getAllCRSes(
		@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
	    @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
		@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		CRSResults	results = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			List<CRS> list = catalog.getAllCRSes();
			if(list != null) {
				if ((offset < 0) || ((list.size() > 0) && (offset >= list.size())))
					throw new IndexOutOfBoundsException("The start index is out of the range");

				if ((limit < 0) || (limit > list.size() - offset))
					limit = list.size() - offset;

				if (limit >= 0) {
					ArrayList<CRS> list2 = new ArrayList<>();
					for (int i = offset; i < offset + limit; i++) {
						list2.add((CRS)list.get(i).convert(mode));
					}
					results = new CRSResults(list2, offset, list.size());
				}
			}
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(results)) {
			this.auditLogger.readCrsSuccess(Collections.singletonList(results.toString()));
		} else {
			this.auditLogger.readCrsSuccess(Collections.singletonList("CRSResults is null"));
		}
		return results;
	}

    /**
     * get the CRS by essence
     * @param request The CRSRequest object for the CRS
     * @return CRS
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/crs")
	public CRS getCRS(@RequestBody CRSRequest request,
					  @RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		CRS	crs = null;

		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			CRSEssence essence = request.getCRSEssenceJson();
			if (essence == null) {
				EssenceImpl ei = EssenceImpl.createInstance(request.getPersistableReference());
				essence = (CRSEssence) ei;
				if (essence == null) throw new IllegalArgumentException("Malformed persistableReference");
			}
			crs = catalog.getCRS(essence);
			if(null != crs){
				crs = (CRS)crs.convert(mode);
			}
		}
		catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(crs)) {
			this.auditLogger.readCrsByEssenceSuccess(Collections.singletonList(crs.toString()));
		} else {
			this.auditLogger.readCrsByEssenceSuccess(Collections.singletonList("CRS is null"));
		}
		return crs;
	}

    /**
     * get a list of LateBoundCRSes in the catalog
     * @param offset  Offset of the first item in the LateBoundCRSes. It is optional and is 0 by default.
     * @param limit   Maximum number of the LateBoundCRSes items returned. It is optional and is 100 by default.
     * @return        LateBoundCRSResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@GetMapping("/lateboundcrs")
	public LateBoundCRSResults getLateBoundCRSes(
		@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
	    @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
		@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		LateBoundCRSResults	results = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			List<LateBoundCRS> list = catalog.getLateBoundCRSes();
			if(list != null) {
				if ((offset < 0) || ((list.size() > 0) && (offset >= list.size())))
					throw new IndexOutOfBoundsException("The start index is out of the range");

				if ((limit < 0) || (limit > list.size() - offset))
					limit = list.size() - offset;

				if (limit >= 0) {
					ArrayList<LateBoundCRS> list2 = new ArrayList<>();
					for (int i = offset; i < offset + limit; i++) {
						list2.add(list.get(i).convert(mode));
					}
					results = new LateBoundCRSResults(list2, offset, list.size());
				}
			}
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(results)) {
			this.auditLogger.readLateBoundCrsSuccess(Collections.singletonList(results.toString()));
		} else {
			this.auditLogger.readLateBoundCrsSuccess(Collections.singletonList("LateBoundCRSResults is null"));
		}
		return results;
	}

    /**
     * get the LateBoundCRS by essence
     * @param request LateBoundCRSRequest
     * @return LateBoundCRS
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/lateboundcrs")
	public LateBoundCRS getLateBoundCRS(
		    @RequestBody LateBoundCRSRequest request,
			@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		LateBoundCRSEssence essence = null;
		LateBoundCRS crs  = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			essence = request.getLateBoundCRSEssence();
			if (essence == null) {
				EssenceImpl ei = EssenceImpl.createInstance(request.getPersistableReference());
				essence = (LateBoundCRSEssenceImpl)ei;
			}
			crs = catalog.getLateBoundCRS(essence);
			if(crs != null) {
				crs = crs.convert(mode);
			}
		}
		catch (Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(crs)) {
			this.auditLogger.readLateBoundCrsByEssenceSuccess(Collections.singletonList(crs.toString()));
		} else {
			this.auditLogger.readLateBoundCrsByEssenceSuccess(Collections.singletonList("LateBoundCRS is null"));
		}
		return crs;
	}

    /**
     * get a list of the EarlyBoundCRSes in the catalog
     * @param offset  Offset of the first item in the EarlyBoundCRSes. It is optional and is 0 by default.
     * @param limit   Maximum number of the EarlyBoundCRSes items returned. It is optional and is 100 by default.
     * @return EarlyBoundCRSResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@GetMapping("/earlyboundcrs")
	public EarlyBoundCRSResults getEarlyBoundCRSes(
		@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
	    @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
		@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		EarlyBoundCRSResults	results = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			List<EarlyBoundCRS> list = catalog.getEarlyBoundCRSes();
			if(list != null) {
				if ((offset < 0) || ((list.size() > 0) && (offset >= list.size())))
					throw new IndexOutOfBoundsException("The start index is out of the range");

				if ((limit < 0) || (limit > list.size() - offset))
					limit = list.size() - offset;

				if (limit >= 0) {
					ArrayList<EarlyBoundCRS> list2 = new ArrayList<>();
					for (int i = offset; i < offset + limit; i++) {
						list2.add(list.get(i).convert(mode));
					}
					results =  new EarlyBoundCRSResults(list2, offset, list.size());
				}
			}
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(results)) {
			this.auditLogger.readEarlyBoundCrsSuccess(Collections.singletonList(results.toString()));
		} else {
			this.auditLogger.readEarlyBoundCrsSuccess(Collections.singletonList("EarlyBoundCRSResults is null"));
		}
		return results;
	}

    /**
     * get the EarlyBoundCRS by essence
     * @param request EarlyBoundCRSRequest
     * @return EarlyBoundCRS
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/earlyboundcrs")
	public EarlyBoundCRS getEarlyBoundCRS(@RequestBody EarlyBoundCRSRequest request,
										  @RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		EarlyBoundCRSEssence essence = null;
		EarlyBoundCRS crs = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			essence = request.getEarlyBoundCRSEssence();
			if (essence == null) {
				EssenceImpl ei = EssenceImpl.createInstance(request.getPersistableReference());
				essence = (EarlyBoundCRSEssenceImpl)ei;
			}
			crs = catalog.getEarlyBoundCRS(essence);
			if(crs != null) {
				crs = crs.convert(mode);
			}
		}
		catch (Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(crs)) {
			this.auditLogger.readEarlyBoundCrsByEssenceSuccess(Collections.singletonList(crs.toString()));
		} else {
			this.auditLogger.readEarlyBoundCrsByEssenceSuccess(Collections.singletonList("EarlyBoundCRS is null"));
		}
		return crs;
	}

    /**
     * get a list of the CompoundCRSes in the catalog
     * @param offset  Offset of the first item in the CompoundCRSes. It is optional and is 0 by default.
     * @param limit   Maximum number of the CompoundCRSes items returned. It is optional and is 100 by default.
     * @return CompoundCRSResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@GetMapping("/compoundcrs")
	public CompoundCRSResults getCompoundCRSes(
		@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
	    @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
		@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		CompoundCRSResults	results = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			List<CompoundCRS> list = catalog.getCompoundCRSes();
			if(list != null) {
				if ((offset < 0) || ((list.size() > 0) && (offset >= list.size())))
					throw new IndexOutOfBoundsException("The start index is out of the range");

				if ((limit < 0) || (limit > list.size() - offset))
					limit = list.size() - offset;

				if (limit >= 0) {
					ArrayList<CompoundCRS> list2 = new ArrayList<>();
					for (int i = offset; i < offset + limit; i++) {
						list2.add(list.get(i).convert(mode));
					}
					results = new CompoundCRSResults(list2, offset, list.size());
				}
			}
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(results)) {
			this.auditLogger.readCompoundCrsSuccess(Collections.singletonList(results.toString()));
		} else {
			this.auditLogger.readCompoundCrsSuccess(Collections.singletonList("CompoundCRSResults is null"));
		}
		return results;
	}

    /**
     * get the CompoundCRS by essence
     * @param request CompoundCRSRequest
     * @return CompoundCRS
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/compoundcrs")
	public CompoundCRS getCompoundCRS(@RequestBody CompoundCRSRequest request,
									  @RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		CompoundCRSEssence essence = null;
		CompoundCRS crs = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			essence = request.getCompoundCRSEssence();
			if (essence == null) {
				EssenceImpl ei = EssenceImpl.createInstance(request.getPersistableReference());
				essence = (CompoundCRSEssenceImpl)ei;
			}
			crs = catalog.getCompoundCRS(essence);
			if(crs != null){
				crs = crs.convert(mode);
			}
		}
		catch (Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(crs)) {
			this.auditLogger.readCompoundCrsByEssenceSuccess(Collections.singletonList(crs.toString()));
		} else {
			this.auditLogger.readCompoundCrsByEssenceSuccess(Collections.singletonList("CompoundCRS is null"));
		}
		return crs;
	}

    /**
     * get a list of cartographic transforms (CTs) in the catalog
     * @param offset  Offset of the first item in the cartographic transforms. It is optional and is 0 by default.
     * @param limit   Maximum number of the cartographic transforms items returned. It is optional and is 100 by default.
     * @return CTResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@GetMapping("/ct")
	public CTResults getAllCTs(
		@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
	    @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
		@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		CTResults	results = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			List<CT> list = catalog.getAllCTs();
			if(list != null) {
				if ((offset < 0) || ((list.size() > 0) && (offset >= list.size())))
					throw new IndexOutOfBoundsException("The start index is out of the range");

				if ((limit < 0) || (limit > list.size() - offset))
					limit = list.size() - offset;

				if (limit >= 0) {
					ArrayList<CT> list2 = new ArrayList<>();
					for (int i = offset; i < offset + limit; i++) {
						list2.add((CT)list.get(i).convert(mode));
					}
					results = new CTResults(list2, offset, list.size());
				}
			}
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(results)) {
			this.auditLogger.readCtSuccess(Collections.singletonList(results.toString()));
		} else {
			this.auditLogger.readCtSuccess(Collections.singletonList("CTResults is null"));
		}
		return results;
	}

    /**
     * get the CT by essence
     * @param request CTRequest
     * @return CT
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/ct")
	public CT getCT(@RequestBody CTRequest request,
					@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		CT ct = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			CTEssence essence = request.getCTEssenceJson();
			if (essence == null) {
				EssenceImpl ei = EssenceImpl.createInstance(request.getPersistableReference());
				essence = (CTEssence)ei;
				if (essence == null) throw new IllegalArgumentException("Malformed persistableReference");
			}

			ct = catalog.getCT(essence);
			if(ct != null){
				ct = (CT)ct.convert(mode);
			}
		}
		catch (Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(ct)) {
			this.auditLogger.readCtByEssenceSuccess(Collections.singletonList(ct.toString()));
		} else {
			this.auditLogger.readCtByEssenceSuccess(Collections.singletonList("CTResults is null"));
		}
		return ct;
	}

    /**
     * get a list of SingleCTs in the catalog
     * @param offset  Offset of the first item in the SingleCTs. It is optional and is 0 by default.
     * @param limit   Maximum number of the SingleCTs items returned. It is optional and is 100 by default.
     * @return SingleCTResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@GetMapping("/singlect")
	public SingleCTResults getSingleCTs(
		@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
	    @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
		@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		SingleCTResults	results = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			List<SingleCT> list = catalog.getSingleCTs();
			if(list != null) {
				if ((offset < 0) || ((list.size() > 0) && (offset >= list.size())))
					throw new IndexOutOfBoundsException("The start index is out of the range");

				if ((limit < 0) || (limit > list.size() - offset))
					limit = list.size() - offset;

				if (limit >= 0) {
					ArrayList<SingleCT> list2 = new ArrayList<>();
					for (int i = offset; i < offset + limit; i++) {
						list2.add(list.get(i).convert(mode));
					}
					results = new SingleCTResults(list2, offset, list.size());
				}
			}
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(results)) {
			this.auditLogger.readSingleCtSuccess(Collections.singletonList(results.toString()));
		} else {
			this.auditLogger.readSingleCtSuccess(Collections.singletonList("SingleCTResults is null"));
		}
		return results;
	}

    /**
     * get the SingleCT by essence
     * @param request SingleCTRequest
     * @return SingleCT
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/singlect")
	public SingleCT getSingleCT(@RequestBody SingleCTRequest request,
								@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		SingleCTEssence essence = null;
		SingleCT	ct = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			essence = request.getSingleCTEssence();
			if (essence == null) {
				EssenceImpl ei = EssenceImpl.createInstance(request.getPersistableReference());
				essence = (SingleCTEssence) ei;
			}
			ct = catalog.getSingleCT(essence);
			if(null != ct){
				ct = ct.convert(mode);
			}
		}
		catch (Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(ct)) {
			this.auditLogger.readSingleCtByEssenceSuccess(Collections.singletonList(ct.toString()));
		} else {
			this.auditLogger.readSingleCtByEssenceSuccess(Collections.singletonList("SingleCT is null"));
		}
		return ct;
	}

    /**
     * get a list of CompoundCTs in the catalog
     * @param offset  Offset of the first item in the CompoundCTs. It is optional and is 0 by default.
     * @param limit   Maximum number of the CompoundCTs items returned. It is optional and is 100 by default.
     * @return CompoundCTResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@GetMapping("/compoundct")
	public CompoundCTResults getCompoundCTs(
		@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
	    @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
		@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		CompoundCTResults results = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			List<CompoundCT> list = catalog.getCompoundCTs();
			if(list != null) {
				if ((offset < 0) || ((list.size() > 0) && (offset >= list.size())))
					throw new IndexOutOfBoundsException("The start index is out of the range");

				if ((limit < 0) || (limit > list.size() - offset))
					limit = list.size() - offset;

				if (limit >= 0) {
					ArrayList<CompoundCT> list2 = new ArrayList<>();
					for (int i = offset; i < offset + limit; i++) {
						list2.add(list.get(i).convert(mode));
					}
					results = new CompoundCTResults(list2, offset, list.size());
				}
			}
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(results)) {
			this.auditLogger.readCompoundCtSuccess(Collections.singletonList(results.toString()));
		} else {
			this.auditLogger.readCompoundCtSuccess(Collections.singletonList("CompoundCTResults is null"));
		}
		return results;
	}

   /**
    * get the CompoundCT by essence
    * @param request CompoundCTRequest
    * @return CompoundCT
	* @throws AppException with BadRequest status to indicate a client request is not recognized
	*/
	@PostMapping("/compoundct")
	public CompoundCT getCompoundCT(@RequestBody CompoundCTRequest request,
									@RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		CompoundCTEssence essence = null;
		CompoundCT ct = null;
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			essence = request.getCompoundCTEssence();
			if (essence == null) {
				EssenceImpl ei = EssenceImpl.createInstance(request.getPersistableReference());
				essence = (CompoundCTEssenceImpl)ei;
			}
			ct = catalog.getCompoundCT(essence);
			if(ct != null)
				ct = ct.convert(mode);
		}
		catch (Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
		if (Objects.nonNull(ct)) {
			this.auditLogger.readCompoundCtByEssenceSuccess(Collections.singletonList(ct.toString()));
		} else {
			this.auditLogger.readCompoundCtByEssenceSuccess(Collections.singletonList("CompoundCT is null"));
		}
		return ct;
	}

    /********************************************
     Search related API
     *********************************************/

    /**
	 * Search the AreasOfUse by keyword or bounding box and return the results in given range.
	 * @param request The {@link SearchRequest} containing the search keyword. It could be empty if the bounding box is set.
	 * @param longitudeLeft longitude value of the lower left point of the bounding box. It is option and is -180 degree by default.
	 * @param latitudeLower latitude value of the lower left point of the bounding box. It is option and is -90 degree by default.
	 * @param longitudeRight longitude value of the upper right point of the bounding box. It is option and is 180 degree by default.
	 * @param latitudeUpper latitude value of the upper right point of the bounding box. It is option and is 90 degree by default.
	 * @param offset The offset of the first item in the list of all AreasOfUse. It is optional and is 0 by default.
	 * @param limit The maximum number of the AreasOfUse returned. It is optional and is 100 by default.
	 * @return AreaOfUseResults
     * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/search/area")
    public AreaOfUseResults searchAreasOfUse(@RequestBody SearchRequest request,
											 @RequestParam(name = "longitudeLeft", required = false, defaultValue = "-180") double longitudeLeft,
											 @RequestParam(name = "latitudeLower", required = false, defaultValue = "-90") double latitudeLower,
											 @RequestParam(name = "longitudeRight", required = false, defaultValue = "180")double longitudeRight,
											 @RequestParam(name = "latitudeUpper", required = false, defaultValue = "90") double latitudeUpper,
											 @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
											 @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
											 @RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		try{
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			SphericalBoundingBox boundingBox = new SphericalBoundingBoxImpl(longitudeLeft, latitudeLower, longitudeRight, latitudeUpper);
			AreaOfUseResults results = catalog.searchAreasOfUse(request, boundingBox, offset, limit, mode);
			this.auditLogger.searchAreaSuccess(Collections.singletonList(results.toString()));
			return results;
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
    }

    /**
     * Search the CRSes by keyword and return the results in given range
	 * @param request The {@link SearchRequest} containing the search keyword. It could be empty if the bounding box is set.
	 * @param longitudeLeft longitude value of the lower left point of the bounding box. It is option and is -180 degree by default.
	 * @param latitudeLower latitude value of the lower left point of the bounding box. It is option and is -90 degree by default.
	 * @param longitudeRight longitude value of the upper right point of the bounding box. It is option and is 180 degree by default.
	 * @param latitudeUpper latitude value of the upper right point of the bounding box. It is option and is 90 degree by default.
     * @param offset  The offset of the first item in the list of all CRSes. It is optional and is 0 by default.
     * @param limit   The maximum number of the CRSes returned. It is optional and is 100 by default.
     * @return CRSResults
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/search/crs")
	public CRSResults searchCRSes(@RequestBody SearchRequest request,
								  @RequestParam(name = "longitudeLeft", required = false, defaultValue = "-180") double longitudeLeft,
								  @RequestParam(name = "latitudeLower", required = false, defaultValue = "-90") double latitudeLower,
								  @RequestParam(name = "longitudeRight", required = false, defaultValue = "180") double longitudeRight,
								  @RequestParam(name = "latitudeUpper", required = false, defaultValue = "90") double latitudeUpper,
								  @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
								  @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
								  @RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			SphericalBoundingBox boundingBox = new SphericalBoundingBoxImpl(longitudeLeft, latitudeLower, longitudeRight, latitudeUpper);
			CRSResults results = catalog.searchCRSes(request, boundingBox, offset, limit, mode);
			this.auditLogger.searchCrsSuccess(Collections.singletonList(results.toString()));
			return results;
		}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
    }

	/**
     * Search the SearchCTs by keyword and return the results in given range
	 * @param request The {@link SearchRequest} containing the search keyword. It could be empty if the bounding box is set.
	 * @param longitudeLeft longitude value of the lower left point of the bounding box. It is option and is -180 degree by default.
	 * @param latitudeLower latitude value of the lower left point of the bounding box. It is option and is -90 degree by default.
	 * @param longitudeRight longitude value of the upper right point of the bounding box. It is option and is 180 degree by default.
	 * @param latitudeUpper latitude value of the upper right point of the bounding box. It is option and is 90 degree by default.
     * @param offset  The offset of the first item in the list of all CTs. It is optional and is 0 by default.
     * @param limit   The maximum number of the CTs returned. It is optional and is 100 by default.
     * @return CTResults
	 * @throws AppException with BadRequest status to indicate a client request is not recognized
     */
	@PostMapping("/search/ct")
	public CTResults searchCTs(@RequestBody SearchRequest request,
							   @RequestParam(name = "longitudeLeft", required = false, defaultValue = "-180") double longitudeLeft,
							   @RequestParam(name = "latitudeLower", required = false, defaultValue = "-90") double latitudeLower,
							   @RequestParam(name = "longitudeRight", required = false, defaultValue = "180") double longitudeRight,
							   @RequestParam(name = "latitudeUpper", required = false, defaultValue = "90") double latitudeUpper,
							   @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
							   @RequestParam(name = "limit", required = false, defaultValue = "100") int limit,
							   @RequestParam(name = "mode", required = false, defaultValue = "persistableReference") String reprMode) {
		assertRange(offset, limit);
		try {
			RepresentationMode mode = RepresentationMode.getRepresentationMode(reprMode);
			SphericalBoundingBox boundingBox = new SphericalBoundingBoxImpl(longitudeLeft, latitudeLower, longitudeRight, latitudeUpper);
			CTResults results = catalog.searchCTs(request, boundingBox, offset, limit, mode);
			this.auditLogger.searchCtSuccess(Collections.singletonList(results.toString()));
			return results;
    	}
        catch(Exception ex) {
			log.warning(ex.toString(), ex);
            throw AppException.createBadRequest(ex.getMessage());
		}
    }

    private void assertRange(int offset, int limit) {
        StringBuilder stringBuilder = new StringBuilder();
        if(offset < 0)
            stringBuilder.append("Offset can not be negative. ");

        if(limit < -1)
            stringBuilder.append("'-1' is the only valid negative value and means all. Other negative values for limit is invalid.");

        if(stringBuilder.length() > 0)
            throw AppException.createBadRequest(stringBuilder.toString());
    }
}
