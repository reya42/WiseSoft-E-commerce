import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';

import "./product.css"

const ProductForm = ({isHereTo, method}) => {
    const { currentProductLink } = useParams();
    const [product, setProduct] = useState({});
    const [brands, setBrands] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    // Fetch the current product
    useEffect(() => {
        if (localStorage.getItem("user") == null || !JSON.parse(localStorage.getItem("user")).admin) {
            navigate("/");
        }
        if (isHereTo.includes("update")) {
            const fetchProduct = async () => {
                try {
                    const response = await fetch(`http://localhost:8080/products/admin/${currentProductLink}`);
                    const data = await response.json();
    
                    if (!response.ok) {
                        if (data.error != null) {
                            console.log(data)
                            throw new Error(data.error);
                        } else if (data.message != null) {
                            console.log(data)
                            throw new Error(data.message);
                        }
                    } else {
                        setProduct(data);
                        setLoading(false);
                    }
                } catch (error) {
                    console.error(error.message);
                    setError(error.message);
                    setLoading(false);
                }
            };
            fetchProduct();
        }
    }, [currentProductLink]);

    // Fetch brands and categories
    useEffect(() => {
        const fetchBrandsAndCategories = async () => {
            try {
                const brandResponse = await fetch('http://localhost:8080/brands');
                const brandData = await brandResponse.json();
                setBrands(brandData);

                const categoryResponse = await fetch('http://localhost:8080/categories');
                const categoryData = await categoryResponse.json();
                setCategories(categoryData);
                if (!isHereTo.includes("update")) setLoading(false);
            } catch (error) {
                console.log(error);
                setError(error.message);
            }
        };
        fetchBrandsAndCategories();
    }, []);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        const newValue = type === 'checkbox' ? checked : (type == "number" ? parseFloat(value) : value);
        setProduct((prevProduct) => ({
            ...prevProduct,
            [name]: newValue
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        console.log(JSON.stringify(product));
        if (JSON.stringify(product).brand) {
            
        }
        try {
            const response = await fetch(`http://localhost:8080/${isHereTo}/product`, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(product)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'An error occurred');
            }

            const responseData = await response.json();
            setSuccess('Ürün Başarıyla Güncellendi');
            setError('');
            console.log(responseData);
        } catch (error) {
            setError(error.message);
            setSuccess('');
        }
    };

    if (loading) {
        return <p>Loading...</p>;
    }

    return (
        <div className='updateproductpage'>
            <Link to={"/admin/products"} className='goback'>
                {"<"}Geri Dön
            </Link>
            <form onSubmit={handleSubmit} className='updateproduct'>
                <div className='top'>
                    <div className="left">
                        <div className='updateproduct_brand'>
                            <label htmlFor="brandId">Marka </label>
                            <select
                                id="brandId"
                                name="brand"
                                value={product.brand?.brandId || ''}
                                onChange={(e) => setProduct(prevProduct => ({ ...prevProduct, brand: brands.find(b => b.brandId === parseInt(e.target.value)) }))}
                            >
                                <option value="">Select a Brand</option>
                                {brands.map((brand) => (
                                    <option key={brand.brandId} value={brand.brandId}>{brand.brandName}</option>
                                ))}
                            </select>
                        </div>
                        <div className="updateproduct_category">
                            <label htmlFor="categoryId">Kategori </label>
                            <select
                                id="categoryId"
                                name="category"
                                value={product.category?.categoryId || ''}
                                onChange={(e) => setProduct(prevProduct => ({ ...prevProduct, category: categories.find(c => c.categoryId === parseInt(e.target.value)) }))}
                            >
                                <option value="">Select a Category</option>
                                {categories.map((category) => (
                                    <option key={category.categoryId} value={category.categoryId}>{category.categoryName}</option>
                                ))}
                            </select>
                        </div>
                        <div className="updateproduct_name">
                            <label htmlFor="productName">Ürün Adı </label>
                            <input
                                type="text"
                                id="productName"
                                name="productName"
                                value={product.productName || ''}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="updateproduct_link">
                            <label htmlFor="productLink">Ürün Linki </label>
                            <input
                                type="text"
                                id="productLink"
                                name="productLink"
                                value={product.productLink || ''}
                                onChange={handleChange}
                            />
                        </div>
                        <div className="updateproduct_purchase">
                                    
                            <label htmlFor="productPurchasePrice">Alış Fiyatı </label>
                            <input
                                type="number"
                                id="productPurchasePrice"
                                name="productPurchasePrice"
                                value={product.productPurchasePrice || 0}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="updateproduct_sell">
                            <label htmlFor="productSellPrice">Satış Fiyatı</label>
                            <input
                                type="number"
                                id="productSellPrice"
                                name="productSellPrice"
                                value={product.productSellPrice || 0}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="updateproduct_discount">
                            <label htmlFor="productDiscountedPrice">İndirimli Satış Fiyatı</label>
                            <input
                                type="number"
                                id="productDiscountedPrice"
                                name="productDiscountedPrice"
                                value={product.productDiscountedPrice || 0}
                                onChange={handleChange}
                            />
                        </div>
                        <div className="updateproduct_stock">
                            <label htmlFor="productStock">Stok</label>
                            <input
                                type="number"
                                id="productStock"
                                name="productStock"
                                value={product.productStock || 0}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="updateproduct_standout">
                            <label htmlFor="standOutRowNum">Öne Çıkartma Sayısı</label>
                            <input
                                type="number"
                                id="standOutRowNum"
                                name="standOutRowNum"
                                value={product.standOutRowNum || 0}
                                onChange={handleChange}
                            />
                        </div>
                        <div className="updateproduct_shippingfee">
                            <label htmlFor="productShippingFee">Gönderim Ücreti</label>
                            <input
                                type="number"
                                id="productShippingFee"
                                name="productShippingFee"
                                value={product.productShippingFee || 0}
                                onChange={handleChange}
                            />
                        </div>
                    </div>
                    <div className="transverse-mid">
                        <div className="updateproduct_isactive">
                            <label>
                                <input
                                    type="checkbox"
                                    name="productActive"
                                    checked={product.productActive || false}
                                    onChange={handleChange}
                                />
                                Ürün Aktif
                            </label>
                        </div>
                        <div className="updateproduct_displayonhome">
                            <label>
                                <input
                                    type="checkbox"
                                    name="displayProductOnHomePage"
                                    checked={product.displayProductOnHomePage || false}
                                    onChange={handleChange}
                                />
                                Ana Sayfada Gösteriliyor
                            </label>
                        </div>
                        <div className="updateproduct_isnew">
                            <label>
                                <input
                                    type="checkbox"
                                    name="isNew"
                                    checked={product.isNew || false}
                                    onChange={handleChange}
                                />
                                Ürün Yeni
                            </label>
                        </div>
                        <div className="updateproduct_hero">
                            <label>
                                <input
                                    type="checkbox"
                                    name="heroProduct"
                                    checked={product.heroProduct || false}
                                    onChange={handleChange}
                                />
                                Çok Satanlarda Bulunuyor
                            </label>
                        </div>
                        <div className="updateproduct_isthereshipping">
                            <label>
                                <input
                                    type="checkbox"
                                    name="thereShippingFee"
                                    checked={product.thereShippingFee || false}
                                    onChange={handleChange}
                                />
                                Gönderim Ücreti İçeriyor
                            </label>
                        </div>
                    </div>
                </div>
                <div className='bottom'>
                    <div className='left'>
                        <div className="updateproduct_desc">
                            <label htmlFor="productDescription">Açıklama</label>
                            <textarea
                                id="productDescription"
                                name="productDescription"
                                value={product.productDescription || ''}
                                onChange={handleChange}
                            ></textarea>
                        </div>
                        <div className="updateproduct_specs">
                            <label htmlFor="productSpecs">Özellikler</label>
                            <textarea
                                id="productSpecs"
                                name="productSpecs"
                                value={product.productSpecs || ''}
                                onChange={handleChange}
                            ></textarea>
                        </div>
                    </div>
                    <div className="right">
                        <div className="updateproduct_inthebox">
                            <textarea
                                id="productWhatsInTheBox"
                                name="productWhatsInTheBox"
                                value={product.productWhatsInTheBox || ''}
                                onChange={handleChange}
                            ></textarea>
                            <label htmlFor="productWhatsInTheBox">Kutuda Neler Var</label>
                        </div>
                        <button type="submit">
                            {
                                isHereTo.includes("update")?
                                    "Ürünü Güncelle"
                                :
                                    "Ürün Oluştur"
                            }
                        </button>
                    </div>
                </div>
                {error && <p className='admin_error' onClick={() => setError("")}>{error}</p>}
                {success && <p className='admin_success' onClick={() => setSuccess("")}>{success}</p>}
            </form>
        </div>
    );
};

export default ProductForm;