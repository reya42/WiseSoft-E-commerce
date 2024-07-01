import React, { useEffect, useState } from 'react'
import "./css/categories.css"
import { useParams, Link, useNavigate as navigate } from 'react-router-dom'

import Product from '../components/Product'

const Categories = () => {
  const { categoryLink } = useParams();
  const [curCategory, setCurCategory] = useState();
  const [categories, setCategories] = useState([]);
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const [products, setProducts] = useState([]);
  const [error, setError] = useState();

  const [currentPage,setCurrentPage] = useState(0);

  useEffect(() => {
      const fetchCategoryAndProducts = async () => {
          try {
              // Kategori bilgilerini çek
              const categoryResponse = await fetch(`http://localhost:8080/category/${categoryLink}`);
              if (categoryResponse.status === 404) {
                setError(categoryResponse.text());
                throw new Error('Category not found');
              }
              if (!categoryResponse.ok) {
                  throw new Error('Network response was not ok for category');
              }
              const categoryData = await categoryResponse.json();
              if (categoryData.message) {
                  setError(categoryData.message);
              }
              setCurCategory(categoryData); // Kategori state'ini ayarla

              // Kategori için ürünleri çek
              const productsResponse = await fetch(`http://localhost:8080/category/${categoryLink}/products?page=${currentPage}`);
              if (productsResponse.status === 404) {
                setError(productsResponse.text());
                  throw new Error('Category products not found');
              }
              if (!productsResponse.ok) {
                  throw new Error('Network response was not ok for products');
              }
              const productsData = await productsResponse.json();
              if (productsData.message)
              {
                throw new Error(productsData.message);
              }
              else
              {
                setProducts(productsData);
              }
          } catch (error) {
              setError(error.message);
          }
      };

      if (categoryLink) {
          fetchCategoryAndProducts();
      }
  }, [categoryLink,currentPage]);

  useEffect(()=>{
    const fetchCategories = async () => {
      try {
          const response = await fetch('http://localhost:8080/categories/parent-to-children');
          if (!response.ok) {
              throw new Error('Network response was not ok');
          }
          const data = await response.json();
          setCategories(data);
          setCategoriesLoading(false);
      } catch (error) {
          console.error('Error fetching categories:', error);
      }
  };

  fetchCategories();
  },[])

  const handlePage = (add) => {
    if (add && !products.last) {
        setCurrentPage(currentPage+1);
    }
    else if (!add && !products.first) {
        setCurrentPage(currentPage-1);
    }
  }

  if (error) {
      return (
          <div className="categories-error">
              {error.toString()}
          </div>
      );
  }

  if (categoriesLoading) {
      return (
          <div className="categories-page-container">
              <div className="categories-data">
                  <div className="category-name">
                      Categories Loading...
                  </div>
                  <div className="child-categories">

                  </div>
              </div>
              <div className="category-product-chunk-container">
                  <div className="products-container">

                  </div>
              </div>
              <div className="page-switcher">

              </div>
          </div>
      );
  } else {
      return (
          <div className="categories-page-container">
              <div className="categories-data">
                  <div className="categories-title">
                      Kategoriler
                  </div>
                  <div className="all-categories">
                      {
                        categories.length > 0 && (
                            categories.map((rootCategory, rootIndex) => (
                                <>
                                    <div>
                                            <Link className="root-categories"
                                                to={`/categories/${rootCategory.categoryLink}`}>
                                                
                                                {rootCategory.categoryName}
                                            </Link>
                                        </div>
                                        {
                                            rootCategory.childrenCategories.length > 0 && (
                                                rootCategory.childrenCategories.map((childCategory, childIndex) => (
                                                    <>
                                                        <div>
                                                        <Link className="child-categories"
                                                                to={`/categories/${childCategory.categoryLink}`}>
                                                            {childCategory.categoryName}
                                                        </Link>
                                                        </div>
                                                        {
                                                            childCategory.childrenCategories.length > 0 && (
                                                                childCategory.childrenCategories.map((grandChildCategory, childIndex) => (
                                                                    <div>
                                                                    <Link className="grand-children-categories"
                                                                            to={`/categories/${grandChildCategory.categoryLink}`}>
                                                                        
                                                                        {grandChildCategory.categoryName}
                                                                    </Link>
                                                                    </div>
                                                                ))
                                                            )
                                                        }
                                                    </>
                                                ))
                                            )
                                        }
                                </>
                            ))
                        )
                      }
                  </div>
              </div>
              <div className="product-chunk-container">
                  <div className="category-name">
                      {curCategory && curCategory.categoryName}
                  </div>
                  <div className="products-container">
                      {
                          products.content && products.content.length > 0 && (

                            products.content.reduce((resultArray, item, index) => {
                                  const chunkIndex = Math.floor(index / 6)

                                  if (!resultArray[chunkIndex]) {
                                      resultArray[chunkIndex] = []
                                  }

                                  resultArray[chunkIndex].push(
                                      <Product product={item} theme={true} width={"14%"} />
                                  )

                                  return resultArray
                            }, []).map((brandsDataChunk, index) => (
                                  <div className='categories-products-chunk' key={`brand-chunk-${index}`}>
                                      {brandsDataChunk}
                                  </div>
                                )
                              )
                        )
                      }
                  </div>
                  {
                    products.totalPages > 1 &&
                    (
                        <div className="page-changer-container">
                            <div className='page-changer-with-arrows'>
                                <div className={`page-button-arrow ${!products.first ? "active":""}`} onClick={() => handlePage(false)}>
                                    {"<"}
                                </div>
                                <div className="page-counter">
                                    {currentPage}
                                </div>
                                <div className={`page-button-arrow ${!products.last ? "active":""}`} onClick={() => handlePage(true)}>
                                    {">"}
                                </div>
                            </div>
                        </div>
                    )
                  }
              </div>
          </div>
      );
  }
}

export default Categories